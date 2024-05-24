import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

class Usuario {
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private Endereco endereco;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }
}

class Endereco {
    private String estado;
    private String cidade;
    private String rua;
    private String numero;
    private String cep;

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getRua() { return rua; }
    public void setRua(String rua) { this.rua = rua; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
}

class Consulta {
    private Date dataHora;
    private Usuario paciente;

    public Date getDataHora() { return dataHora; }
    public void setDataHora(Date dataHora) { this.dataHora = dataHora; }

    public Usuario getPaciente() { return paciente; }
    public void setPaciente(Usuario paciente) { this.paciente = paciente; }
}

class Sistema {
    List<Usuario> usuarios = new ArrayList<>();
    List<Endereco> enderecos = new ArrayList<>();
    List<Consulta> consultas = new ArrayList<>();

    void inserirUsuario(Usuario usuario) { usuarios.add(usuario); }
    void editarUsuario(Usuario usuario, String nome, String cpf, String email, String telefone, Endereco endereco) {
        usuario.setNome(nome);
        usuario.setCpf(cpf);
        usuario.setEmail(email);
        usuario.setTelefone(telefone);
        usuario.setEndereco(endereco);
    }
    void deletarUsuario(Usuario usuario) { usuarios.remove(usuario); }
    void inserirEndereco(Endereco endereco) { enderecos.add(endereco); }
    void editarEndereco(Endereco endereco, String estado, String cidade, String rua, String numero, String cep) {
        endereco.setEstado(estado);
        endereco.setCidade(cidade);
        endereco.setRua(rua);
        endereco.setNumero(numero);
        endereco.setCep(cep);
    }
    void deletarEndereco(Endereco endereco) { enderecos.remove(endereco); }
    void inserirConsulta(Consulta consulta) { consultas.add(consulta); }
    void editarConsulta(Consulta consulta, Date dataHora, Usuario paciente) {
        consulta.setDataHora(dataHora);
        consulta.setPaciente(paciente);
    }
    void deletarConsulta(Consulta consulta) { consultas.remove(consulta); }

    public Endereco buscarEnderecoPorCep(String cep) throws IOException {
        String url = "http://viacep.com.br/ws/" + cep + "/json/";
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();

        String json = content.toString();
        return parseJsonToEndereco(json);
    }

    private Endereco parseJsonToEndereco(String json) {
        String[] parts = json.replace("{", "").replace("}", "").replace("\"", "").split(",");
        Map<String, String> map = new HashMap<>();
        for (String part : parts) {
            String[] kv = part.split(":");
            map.put(kv[0], kv[1]);
        }
        Endereco endereco = new Endereco();
        endereco.setCep(map.get("cep"));
        endereco.setEstado(map.get("uf"));
        endereco.setCidade(map.get("localidade"));
        endereco.setRua(map.get("logradouro"));
        return endereco;
    }
}

public class Main {
    public static void main(String[] args) {
        Sistema sistema = new Sistema();
        Scanner scanner = new Scanner(System.in);
        SimpleDateFormat formatoDataHora = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/clinica", "postgres", "postgres");
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("CREATE INDEX idx_nome ON usuarios(nome)");
            statement.executeUpdate("CREATE INDEX idx_cpf ON usuarios(cpf)");

            String triggerQuery = "CREATE TRIGGER before_insert_consultas " +
                    "BEFORE INSERT ON consultas " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   IF NEW.dataHora < NOW() THEN " +
                    "       SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'A data da consulta não pode ser no passado'; " +
                    "   END IF; " +
                    "END;";

            statement.executeUpdate(triggerQuery);

            statement.executeUpdate("CREATE TABLE auditoria (id INT AUTO_INCREMENT PRIMARY KEY, operacao VARCHAR(50), data TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            String triggerAuditoria = "CREATE TRIGGER after_insert_usuario " +
                    "AFTER INSERT ON usuarios " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   INSERT INTO auditoria (operacao) VALUES ('INSERT'); " +
                    "END;";
            statement.executeUpdate(triggerAuditoria);

            triggerAuditoria = "CREATE TRIGGER after_update_usuario " +
                    "AFTER UPDATE ON usuarios " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   INSERT INTO auditoria (operacao) VALUES ('UPDATE'); " +
                    "END;";
            statement.executeUpdate(triggerAuditoria);

            triggerAuditoria = "CREATE TRIGGER after_delete_usuario " +
                    "AFTER DELETE ON usuarios " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   INSERT INTO auditoria (operacao) VALUES ('DELETE'); " +
                    "END;";
            statement.executeUpdate(triggerAuditoria);

            System.out.println("Índices, trigger de consulta e auditoria criados com sucesso.");

            statement.executeUpdate("CREATE VIEW agenda_pacientes AS " +
                    "SELECT c.dataHora, u.nome AS paciente, " +
                    "CASE " +
                    "   WHEN c.status = 'C' THEN 'Cancelada' " +
                    "   WHEN c.status = 'C' THEN 'Confirmada' " +
                    "   WHEN c.status = 'R' THEN 'Reagendada' " +
                    "END AS status " +
                    "FROM consultas c " +
                    "INNER JOIN usuarios u ON c.paciente_id = u.id");

            System.out.println("View 'agenda_pacientes' criada com sucesso.");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        while (true) {
            System.out.println("Escolha uma opção:");
            System.out.println("1. Inserir usuário");
            System.out.println("2. Editar usuário");
            System.out.println("3. Deletar usuário");
            System.out.println("4. Listar usuários");
            System.out.println("5. Criar consulta");
            System.out.println("6. Listar consultas");
            System.out.println("7. Gerenciar consulta");
            System.out.println("8. Consultar agenda de pacientes");
        
            String opcao = scanner.nextLine();
        
            switch (opcao) {
                case "1":
                    Usuario usuario = new Usuario();
                    Endereco endereco = new Endereco();
                    System.out.println("Digite o nome do usuário:");
                    usuario.setNome(scanner.nextLine());
                    System.out.println("Digite o CPF do usuário:");
                    usuario.setCpf(scanner.nextLine());
                    System.out.println("Digite o email do usuário:");
                    usuario.setEmail(scanner.nextLine());
                    System.out.println("Digite o telefone do usuário:");
                    usuario.setTelefone(scanner.nextLine());
                    System.out.println("Digite o CEP do endereço do usuário:");
                    String cep = scanner.nextLine();
                    try {
                        endereco = sistema.buscarEnderecoPorCep(cep);
                        System.out.println("Digite o número do endereço do usuário:");
                        endereco.setNumero(scanner.nextLine());
                        usuario.setEndereco(endereco);
                        sistema.inserirUsuario(usuario);
                        System.out.println("Usuário inserido com sucesso.");
                    } catch (IOException e) {
                        System.out.println("Erro ao buscar endereço: " + e.getMessage());
                    } catch (SQLException e) {
                        System.out.println("Erro ao inserir usuário: " + e.getMessage());
                    }
                    break;
                case "2":
                    System.out.println("Digite o CPF do usuário que deseja editar:");
                    String cpf = scanner.nextLine();
                    for (Usuario u : sistema.usuarios) {
                        if (u.getCpf().equals(cpf)) {
                            System.out.println("Digite o novo nome do usuário:");
                            u.setNome(scanner.nextLine());
                            System.out.println("Digite o novo email do usuário:");
                            u.setEmail(scanner.nextLine());
                            System.out.println("Digite o novo telefone do usuário:");
                            u.setTelefone(scanner.nextLine());
                            System.out.println("Digite o novo CEP do endereço do usuário:");
                            cep = scanner.nextLine();
                            try {
                                Endereco novoEndereco = sistema.buscarEnderecoPorCep(cep);
                                System.out.println("Digite o novo número do endereço do usuário:");
                                novoEndereco.setNumero(scanner.nextLine());
                                u.setEndereco(novoEndereco);
                                System.out.println("Usuário editado com sucesso.");
                            } catch (IOException e) {
                                System.out.println("Erro ao buscar endereço: " + e.getMessage());
                            }
                            break;
                        }
                    }
                    break;
                case "3":
                    System.out.println("Digite o CPF do usuário que deseja deletar:");
                    cpf = scanner.nextLine();
                    Usuario usuarioParaDeletar = null;
                    for (Usuario u : sistema.usuarios) {
                        if (u.getCpf().equals(cpf)) {
                            usuarioParaDeletar = u;
                            break;
                        }
                    }
                    if (usuarioParaDeletar != null) {
                        sistema.deletarUsuario(usuarioParaDeletar);
                        System.out.println("Usuário deletado.");
                    } else {
                        System.out.println("Usuário não encontrado.");
                    }
                    break;
                case "4":
                    System.out.println("Usuários cadastrados:");
                    for (Usuario u : sistema.usuarios) {
                        System.out.println("Nome: " + u.getNome() + ", CPF: " + u.getCpf());
                    }
                    break;
                case "5":
                    Consulta consulta = new Consulta();
                    System.out.println("Digite a data e a hora da consulta (dd/MM/yyyy HH:mm):");
                    try {
                        consulta.setDataHora(formatoDataHora.parse(scanner.nextLine()));
                    } catch (ParseException e) {
                        System.out.println("Formato de data inválido.");
                        break;
                    }
                    System.out.println("Usuários disponíveis:");
                    for (int i = 0; i < sistema.usuarios.size(); i++) {
                        System.out.println((i + 1) + ". " + sistema.usuarios.get(i).getNome());
                    }
                    System.out.println("Digite o número do paciente:");
                    int indicePaciente = Integer.parseInt(scanner.nextLine()) - 1;
                    if (indicePaciente < 0 || indicePaciente >= sistema.usuarios.size()) {
                        System.out.println("Número de paciente inválido.");
                        break;
                    }
                    consulta.setPaciente(sistema.usuarios.get(indicePaciente));
                    sistema.inserirConsulta(consulta);
                    System.out.println("Consulta criada.");
                    break;
                    case "6":
                    System.out.println("Consultas marcadas:");
                    for (Consulta c : sistema.consultas) {
                        System.out.println("Data e hora: " + formatoDataHora.format(c.getDataHora()) + ", Paciente: " + c.getPaciente().getNome());
                    }
                    break;
                case "7":
                    System.out.println("Consultas marcadas:");
                    for (int i = 0; i < sistema.consultas.size(); i++) {
                        System.out.println((i + 1) + ". " + formatoDataHora.format(sistema.consultas.get(i).getDataHora()) + ", Paciente: " + sistema.consultas.get(i).getPaciente().getNome());
                    }
                    System.out.println("Digite o número da consulta que deseja gerenciar:");
                    int indiceConsulta = Integer.parseInt(scanner.nextLine()) - 1;
                    if (indiceConsulta < 0 || indiceConsulta >= sistema.consultas.size()) {
                        System.out.println("Número de consulta inválido.");
                        break;
                    }
                    Consulta consulta1 = sistema.consultas.get(indiceConsulta);
                    System.out.println("Escolha uma opção:");
                    System.out.println("1. Cancelar consulta");
                    System.out.println("2. Reagendar consulta");
                    System.out.println("3. Confirmar consulta");
                    String opcaoConsulta = scanner.nextLine();
                    switch (opcaoConsulta) {
                        case "1":
                            sistema.deletarConsulta(consulta1);
                            System.out.println("Consulta cancelada.");
                            break;
                        case "2":
                            System.out.println("Digite a nova data e hora da consulta (dd/MM/yyyy HH:mm):");
                            try {
                                consulta1.setDataHora(formatoDataHora.parse(scanner.nextLine()));
                                System.out.println("Consulta reagendada.");
                            } catch (ParseException e) {
                                System.out.println("Formato de data inválido.");
                            }
                            break;
                        case "3":
                            System.out.println("Consulta confirmada.");
                            break;
                        default:
                            System.out.println("Opção inválida.");
                    }
                    break;
                    case "8":
                    System.out.println("Consultas marcadas:");
                    for (int i = 0; i < sistema.consultas.size(); i++) {
                        Consulta c = sistema.consultas.get(i);
                        System.out.println((i + 1) + ". Data e hora: " + formatoDataHora.format(c.getDataHora()) + ", Paciente: " + c.getPaciente().getNome());
                    }
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        }
    }
}
