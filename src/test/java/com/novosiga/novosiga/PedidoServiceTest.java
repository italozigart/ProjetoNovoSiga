package com.novosiga.novosiga;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import com.novosiga.novosiga.dto.FiltroLoja;
import com.novosiga.novosiga.entity.Aluno;
import com.novosiga.novosiga.entity.Atletica;
import com.novosiga.novosiga.entity.Curso;
import com.novosiga.novosiga.entity.Pedido;
import com.novosiga.novosiga.entity.Produto;
import com.novosiga.novosiga.exception.NegocioException;
import com.novosiga.novosiga.repository.AlunoRepository;
import com.novosiga.novosiga.repository.AtleticaRepository;
import com.novosiga.novosiga.repository.CursoRepository;
import com.novosiga.novosiga.repository.ProdutoRepository;
import com.novosiga.novosiga.service.AlunoService;
import com.novosiga.novosiga.service.PedidoService;
import com.novosiga.novosiga.service.ProdutoService;

/**
 * Testes da finalização de compra e da busca, usando o banco H2 em memória
 * (configurado em src/test/resources/application.properties).
 * @Transactional desfaz tudo ao final de cada teste.
 */
@SpringBootTest
@Transactional
class PedidoServiceTest {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private AlunoService alunoService;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private AtleticaRepository atleticaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    private Aluno aluno;
    private Produto camiseta;
    private Produto caneca;

    @BeforeEach
    void prepararDados() {
        Curso curso = new Curso();
        curso.setNomeCurso("Sistemas para Internet");
        curso.setDescricaoCurso("Curso de teste");
        curso.setCargaHoraria(2400);
        curso = cursoRepository.save(curso);

        aluno = new Aluno();
        aluno.setNomeAluno("João da Silva");
        aluno.setEnderecoAluno("Rua A, 10");
        aluno.setBairroAluno("Centro");
        aluno.setCidadeAluno("Jales");
        aluno.setEstadoAluno("sp");
        aluno.setCepAluno("15700-000");
        aluno.setTelefoneAluno("(17) 99999-0000");
        aluno.setCpfAluno("123.456.789-00");
        aluno.setCurso(curso);
        aluno = alunoRepository.save(aluno);

        Atletica atletica = new Atletica();
        atletica.setNomeAtletica("Atlética Teste");
        atletica = atleticaRepository.save(atletica);

        camiseta = produtoRepository.save(produto("Camiseta Calça Preta", 50.0, 5, "Camiseta", atletica));
        caneca = produtoRepository.save(produto("Caneca 350 ml", 30.0, 0, "Caneca", atletica));
    }

    @Test
    void finalizarCompraGravaPedidoEBaixaEstoque() {
        Pedido pedido = pedidoService.finalizarCompra(aluno.getIdAluno(), "PIX",
                Map.of(camiseta.getIdProduto(), 2));

        assertThat(pedido.getIdPedido()).isNotNull();
        assertThat(pedido.getTotalPedido()).isEqualTo(100.0);
        assertThat(pedido.getItens()).hasSize(1);
        assertThat(produtoRepository.findById(camiseta.getIdProduto()).orElseThrow().getEstoqueProduto())
                .isEqualTo(3);
    }

    @Test
    void naoVendeMaisDoQueOEstoque() {
        assertThatThrownBy(() -> pedidoService.finalizarCompra(aluno.getIdAluno(), "PIX",
                Map.of(camiseta.getIdProduto(), 6)))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("Estoque insuficiente");
    }

    @Test
    void exigeFormaDePagamentoValida() {
        assertThatThrownBy(() -> pedidoService.finalizarCompra(aluno.getIdAluno(), "Fiado",
                Map.of(camiseta.getIdProduto(), 1)))
                .isInstanceOf(NegocioException.class);
    }

    @Test
    void buscaIgnoraAcentosEMaiusculas() {
        Page<Produto> resultado = produtoService.buscar("CALCA preta", 0, 10);
        assertThat(resultado.getContent()).extracting(Produto::getDescricaoProduto)
                .containsExactly("Camiseta Calça Preta");
    }

    @Test
    void buscaDeAlunoPorCpfComPontuacao() {
        assertThat(alunoService.buscar("456.789", 0, 10).getContent())
                .extracting(Aluno::getNomeAluno).contains("João da Silva");
        assertThat(alunoService.buscar("joao", 0, 10).getContent())
                .extracting(Aluno::getNomeAluno).contains("João da Silva");
    }

    @Test
    void filtroDaLojaSomenteDisponiveis() {
        FiltroLoja filtro = new FiltroLoja();
        filtro.setDisponiveis(true);

        assertThat(produtoService.buscarNaLoja(filtro, 0, 12).getContent())
                .extracting(Produto::getDescricaoProduto)
                .contains("Camiseta Calça Preta")
                .doesNotContain("Caneca 350 ml");
    }

    private Produto produto(String descricao, double valor, int estoque, String categoria, Atletica atletica) {
        Produto produto = new Produto();
        produto.setDescricaoProduto(descricao);
        produto.setMarcaProduto("Atlética Teste");
        produto.setModeloProduto("Único");
        produto.setValorProduto(valor);
        produto.setEstoqueProduto(estoque);
        produto.setCategoriaProduto(categoria);
        produto.setAtletica(atletica);
        return produto;
    }
}
