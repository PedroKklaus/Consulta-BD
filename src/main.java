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
}

public class main {
    public static void main(String[] args) {
        Sistema sistema = new Sistema();
        Scanner scanner = new Scanner(System.in);
        SimpleDateFormat formatoDataHora = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        while (true) {
            System.out.println("Escolha uma opção:");
            System.out.println("1. Inserir usuário");
            System.out.println("2. Editar usuário");
            System.out.println("3. Deletar usuário");
            System.out.println("4. Listar usuários");
            System.out.println("5. Criar consulta");
            System.out.println("6. Listar consultas");
            System.out.println("7. Gerenciar consulta");

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
                    System.out.println("Digite o estado do endereço do usuário:");
                    endereco.setEstado(scanner.nextLine());
                    System.out.println("Digite a cidade do endereço do usuário:");
                    endereco.setCidade(scanner.nextLine());
                    System.out.println("Digite a rua do endereço do usuário:");
                    endereco.setRua(scanner.nextLine());
                    System.out.println("Digite o número do endereço do usuário:");
                    endereco.setNumero(scanner.nextLine());
                    System.out.println("Digite o CEP do endereço do usuário:");
                    endereco.setCep(scanner.nextLine());
                    usuario.setEndereco(endereco);
                    sistema.inserirUsuario(usuario);
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
                            // Adicione mais campos conforme necessário
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
                case "6": // Nova opção para listar consultas
                    System.out.println("Consultas marcadas:");
                    for (Consulta c : sistema.consultas) {
                        System.out.println("Data e hora: " + formatoDataHora.format(c.getDataHora()) + ", Paciente: " + c.getPaciente().getNome());
                    }
                    break;
                case "7": // Nova opção para gerenciar consultas
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
                default:
                    System.out.println("Opção inválida.");
            }
        }
    }
}