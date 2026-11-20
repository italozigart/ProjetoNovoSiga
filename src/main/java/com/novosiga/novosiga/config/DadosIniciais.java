package com.novosiga.novosiga.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.novosiga.novosiga.entity.Aluno;
import com.novosiga.novosiga.entity.Atletica;
import com.novosiga.novosiga.entity.Curso;
import com.novosiga.novosiga.entity.Disciplina;
import com.novosiga.novosiga.entity.Produto;
import com.novosiga.novosiga.entity.Professor;
import com.novosiga.novosiga.entity.Usuario;
import com.novosiga.novosiga.repository.AlunoRepository;
import com.novosiga.novosiga.repository.AtleticaRepository;
import com.novosiga.novosiga.repository.CursoRepository;
import com.novosiga.novosiga.repository.DisciplinaRepository;
import com.novosiga.novosiga.repository.ProdutoRepository;
import com.novosiga.novosiga.repository.ProfessorRepository;
import com.novosiga.novosiga.repository.UsuarioRepository;

/**
 * Cria dados de exemplo na primeira execução, para o sistema já abrir com
 * alunos, disciplinas e produtos na loja.
 *
 * Regras (nada do que você já cadastrou é alterado):
 *  - usuário admin/admin123: só se não existir nenhum usuário;
 *  - cursos, professores, disciplinas e alunos: só se não existir nenhum curso;
 *  - atléticas e produtos: só se não existir nenhuma atlética e nenhum produto.
 *
 * Para desligar: novosiga.dados-exemplo=false no application.properties.
 */
@Component
public class DadosIniciais implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosIniciais.class);

    @Value("${novosiga.dados-exemplo:false}")
    private boolean criarDadosExemplo;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private AtleticaRepository atleticaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (!criarDadosExemplo) {
            return;
        }
        criarUsuarioAdmin();
        List<Curso> cursos = criarDadosAcademicos();
        criarLoja(cursos);
    }

    // ==========================================================
    // USUÁRIO
    // ==========================================================

    private void criarUsuarioAdmin() {
        if (usuarioRepository.count() > 0) {
            return;
        }
        Usuario admin = new Usuario();
        admin.setNomeUsuario("Administrador");
        admin.setCpfUsuario("00000000000");
        admin.setLoginUsuario("admin");
        admin.setSenhaUsuario(passwordEncoder.encode("admin123"));
        admin.setRole("ROLE_USER");
        usuarioRepository.save(admin);
        log.info(">>> Usuário de exemplo criado: login 'admin' e senha 'admin123'");
    }

    // ==========================================================
    // CURSOS, PROFESSORES, DISCIPLINAS E ALUNOS
    // ==========================================================

    private List<Curso> criarDadosAcademicos() {
        if (cursoRepository.count() > 0) {
            return List.of(); // já existem dados acadêmicos: não mexe em nada
        }

        Curso si = cursoRepository.save(curso("Sistemas para Internet",
                "Desenvolvimento de sites, sistemas web e aplicativos", 2400));
        Curso ge = cursoRepository.save(curso("Gestão Empresarial",
                "Gestão de pessoas, processos, finanças e negócios", 2400));
        Curso agro = cursoRepository.save(curso("Agronegócio",
                "Gestão da produção e da cadeia do agronegócio", 2400));

        Professor carlos = professorRepository.save(professor("Carlos Henrique Prado", "10000000001",
                "12.345.678-1", "17999990101", "Rua Sete de Setembro, 1200", "Mestre em Ciência da Computação"));
        Professor fernanda = professorRepository.save(professor("Fernanda Ribeiro Lopes", "10000000002",
                "23.456.789-2", "17999990102", "Avenida João Amadeu, 455", "Doutora em Engenharia de Software"));
        Professor ricardo = professorRepository.save(professor("Ricardo Alves Martins", "10000000003",
                "34.567.890-3", "17999990103", "Rua Nove, 2310", "Mestre em Administração"));

        disciplinaRepository.save(disciplina("Programação Web", "PWEB", 80, si, carlos));
        disciplinaRepository.save(disciplina("Banco de Dados", "BD", 80, si, fernanda));
        disciplinaRepository.save(disciplina("Engenharia de Software", "ES", 80, si, fernanda));
        disciplinaRepository.save(disciplina("Marketing Digital", "MKT", 40, ge, ricardo));
        disciplinaRepository.save(disciplina("Gestão do Agronegócio", "GAG", 80, agro, ricardo));

        alunoRepository.save(aluno("Ana Beatriz Souza", "Rua das Flores, 120", "Centro", "Jales",
                "15700000", "17988880001", "20000000001", si));
        alunoRepository.save(aluno("João Pedro Lima", "Avenida Francisco Jalles, 850", "Jardim Europa", "Jales",
                "15703000", "17988880002", "20000000002", si));
        alunoRepository.save(aluno("Mariana Alves Costa", "Rua Treze, 77", "Centro", "Urânia",
                "15760000", "17988880003", "20000000003", ge));
        alunoRepository.save(aluno("Lucas Ferreira Santos", "Rua Seis, 1450", "Vila Nova", "Santa Fé do Sul",
                "15775000", "17988880004", "20000000004", agro));
        alunoRepository.save(aluno("Beatriz Oliveira Rocha", "Rua Dois, 389", "Jardim do Bosque", "Jales",
                "15705000", "17988880005", "20000000005", si));
        alunoRepository.save(aluno("Rafael Martins Gomes", "Avenida Brasil, 2001", "Centro", "Fernandópolis",
                "15600000", "17988880006", "20000000006", ge));

        log.info(">>> Dados de exemplo criados: 3 cursos, 3 professores, 5 disciplinas e 6 alunos");
        return List.of(si, ge, agro);
    }

    // ==========================================================
    // ATLÉTICAS E PRODUTOS DA LOJA
    // ==========================================================

    private void criarLoja(List<Curso> cursos) {
        if (atleticaRepository.count() > 0 || produtoRepository.count() > 0) {
            return; // já existem atléticas ou produtos: não mexe em nada
        }

        Curso si = cursos.size() > 0 ? cursos.get(0) : null;
        Curso ge = cursos.size() > 1 ? cursos.get(1) : null;
        Curso agro = cursos.size() > 2 ? cursos.get(2) : null;

        Atletica atleticaSi = atleticaRepository.save(atletica("Atlética Sistemas para Internet",
                "Representa o curso de Sistemas para Internet nos jogos universitários e eventos.", si));
        Atletica atleticaGe = atleticaRepository.save(atletica("Atlética Gestão Empresarial",
                "Organiza festas, campeonatos e a integração dos calouros de Gestão.", ge));
        Atletica atleticaAgro = atleticaRepository.save(atletica("Atlética Agronegócio",
                "Leva a força do campo para as competições universitárias.", agro));

        produtoRepository.save(produto("Camiseta Oficial Sistemas 2026", "Atlética SI", "Dry-fit unissex",
                59.90, "Camiseta", 25, atleticaSi));
        produtoRepository.save(produto("Moletom Canguru Sistemas", "Atlética SI", "Algodão com capuz",
                149.90, "Moletom", 10, atleticaSi));
        produtoRepository.save(produto("Caneca Debugger 350 ml", "Atlética SI", "Porcelana",
                34.90, "Caneca", 18, atleticaSi));
        produtoRepository.save(produto("Tirante Sistemas para Internet", "Atlética SI", "Poliéster 2 cm",
                12.00, "Tirante", 40, atleticaSi));

        produtoRepository.save(produto("Camiseta Gestão Empresarial", "Atlética GE", "Algodão unissex",
                49.90, "Camiseta", 30, atleticaGe));
        produtoRepository.save(produto("Copo Long Drink 500 ml", "Atlética GE", "Acrílico",
                19.90, "Copo", 40, atleticaGe));
        produtoRepository.save(produto("Boné Aba Curva Gestão", "Atlética GE", "Ajustável",
                44.90, "Boné", 12, atleticaGe));
        produtoRepository.save(produto("Ingresso Festa de Integração", "Atlética GE", "Lote 1",
                35.00, "Ingresso", 0, atleticaGe));

        produtoRepository.save(produto("Camiseta Agro Raiz", "Atlética Agro", "Algodão unissex",
                49.90, "Camiseta", 20, atleticaAgro));
        produtoRepository.save(produto("Garrafa Térmica Agro 1 L", "Atlética Agro", "Inox",
                89.90, "Garrafa", 6, atleticaAgro));
        produtoRepository.save(produto("Chapéu Agro", "Atlética Agro", "Palha com fita bordada",
                39.90, "Boné", 8, atleticaAgro));
        produtoRepository.save(produto("Chaveiro Abridor Agro", "Atlética Agro", "Metal",
                15.00, "Chaveiro", 50, atleticaAgro));

        log.info(">>> Loja de exemplo criada: 3 atléticas e 12 produtos");
    }

    // ==========================================================
    // MÉTODOS AUXILIARES PARA MONTAR OS OBJETOS
    // ==========================================================

    private Curso curso(String nome, String descricao, int cargaHoraria) {
        Curso curso = new Curso();
        curso.setNomeCurso(nome);
        curso.setDescricaoCurso(descricao);
        curso.setCargaHoraria(cargaHoraria);
        return curso;
    }

    private Professor professor(String nome, String cpf, String rg, String telefone,
            String endereco, String graduacao) {
        Professor professor = new Professor();
        professor.setNomeProfessor(nome);
        professor.setCpfProfessor(cpf);
        professor.setRgProfessor(rg);
        professor.setTelefoneProfessor(telefone);
        professor.setEnderecoProfessor(endereco);
        professor.setGraduacaoProfessor(graduacao);
        return professor;
    }

    private Disciplina disciplina(String nome, String sigla, int cargaHoraria, Curso curso, Professor professor) {
        Disciplina disciplina = new Disciplina();
        disciplina.setNomeDisciplina(nome);
        disciplina.setSiglaDisciplina(sigla);
        disciplina.setCargaHorariaDisciplina(cargaHoraria);
        disciplina.setCurso(curso);
        disciplina.setProfessor(professor);
        return disciplina;
    }

    private Aluno aluno(String nome, String endereco, String bairro, String cidade,
            String cep, String telefone, String cpf, Curso curso) {
        Aluno aluno = new Aluno();
        aluno.setNomeAluno(nome);
        aluno.setEnderecoAluno(endereco);
        aluno.setBairroAluno(bairro);
        aluno.setCidadeAluno(cidade);
        aluno.setEstadoAluno("SP");
        aluno.setCepAluno(cep);
        aluno.setTelefoneAluno(telefone);
        aluno.setCpfAluno(cpf);
        aluno.setCurso(curso);
        return aluno;
    }

    private Atletica atletica(String nome, String descricao, Curso curso) {
        Atletica atletica = new Atletica();
        atletica.setNomeAtletica(nome);
        atletica.setDescricaoAtletica(descricao);
        atletica.setCurso(curso);
        return atletica;
    }

    private Produto produto(String descricao, String marca, String modelo, double valor,
            String categoria, int estoque, Atletica atletica) {
        Produto produto = new Produto();
        produto.setDescricaoProduto(descricao);
        produto.setMarcaProduto(marca);
        produto.setModeloProduto(modelo);
        produto.setValorProduto(valor);
        produto.setCategoriaProduto(categoria);
        produto.setEstoqueProduto(estoque);
        produto.setAtletica(atletica);
        return produto;
    }
}
