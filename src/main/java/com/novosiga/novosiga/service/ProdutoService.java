package com.novosiga.novosiga.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.novosiga.novosiga.busca.Busca;
import com.novosiga.novosiga.dto.FiltroLoja;
import com.novosiga.novosiga.entity.Atletica;
import com.novosiga.novosiga.entity.Produto;
import com.novosiga.novosiga.exception.NegocioException;
import com.novosiga.novosiga.repository.ItemDoPedidoRepository;
import com.novosiga.novosiga.repository.ProdutoRepository;
import com.novosiga.novosiga.util.TextoUtil;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Service
public class ProdutoService {

    // Sugestões exibidas no campo "Categoria" do formulário (é possível digitar outras)
    private static final List<String> CATEGORIAS_SUGERIDAS = List.of(
            "Camiseta", "Moletom", "Regata", "Caneca", "Copo", "Tirante", "Boné",
            "Chaveiro", "Adesivo", "Ingresso", "Garrafa", "Ecobag");

    // injeção de dependência do repositório de produtos
    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ItemDoPedidoRepository itemDoPedidoRepository;

    // método para salvar um produto
    public Produto save(Produto produto) {
        return produtoRepository.save(produto);
    }

    // método para listar todos os produtos (ordem alfabética)
    public List<Produto> findAll() {
        return produtoRepository.findAll(Sort.by("descricaoProduto"));
    }

    // método para buscar produto pelo ID
    public Produto findById(Integer id) {
        return produtoRepository.findById(id).orElse(null);
    }

    public long contar() {
        return produtoRepository.count();
    }

    // Categorias usadas pelos produtos (filtro da loja)
    public List<String> listarCategorias() {
        return produtoRepository.listarCategorias();
    }

    // Sugestões padrão + categorias já cadastradas, sem repetir
    public List<String> categoriasSugeridas() {
        Set<String> todas = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        todas.addAll(CATEGORIAS_SUGERIDAS);
        todas.addAll(produtoRepository.listarCategorias());
        return new ArrayList<>(todas);
    }

    // Busca da listagem de produtos (descrição, marca, modelo, categoria ou atlética)
    public Page<Produto> buscar(String termo, int pagina, int tamanho) {
        Specification<Produto> spec = Busca.porTermo(termo, this::camposDeTexto, null);
        return produtoRepository.findAll(spec,
                PageRequest.of(Math.max(pagina, 0), tamanho, Sort.by("descricaoProduto")));
    }

    // Busca da vitrine da loja: texto + filtros + ordenação
    public Page<Produto> buscarNaLoja(FiltroLoja filtro, int pagina, int tamanho) {
        Specification<Produto> spec = (root, query, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();

            // Texto digitado (cada palavra precisa aparecer em algum campo)
            List<String> palavras = Busca.palavras(filtro.getQ());
            if (!palavras.isEmpty()) {
                List<Expression<String>> campos = camposDeTexto(root);
                for (String palavra : palavras) {
                    condicoes.add(Busca.algumCampoContem(cb, campos, List.of(), palavra));
                }
            }

            // Atlética
            if (filtro.getAtletica() != null) {
                condicoes.add(cb.equal(root.get("atletica").get("idAtletica"), filtro.getAtletica()));
            }

            // Categoria
            if (!TextoUtil.vazio(filtro.getCategoria())) {
                condicoes.add(cb.equal(root.get("categoriaProduto"), filtro.getCategoria()));
            }

            // Faixa de preço
            if (filtro.getPrecoMin() != null) {
                condicoes.add(cb.greaterThanOrEqualTo(root.<Double>get("valorProduto"), filtro.getPrecoMin()));
            }
            if (filtro.getPrecoMax() != null) {
                condicoes.add(cb.lessThanOrEqualTo(root.<Double>get("valorProduto"), filtro.getPrecoMax()));
            }

            // Somente com estoque
            if (filtro.isDisponiveis()) {
                condicoes.add(cb.greaterThan(root.<Integer>get("estoqueProduto"), 0));
            }

            return cb.and(condicoes.toArray(new Predicate[0]));
        };

        return produtoRepository.findAll(spec,
                PageRequest.of(Math.max(pagina, 0), tamanho, ordenacao(filtro.getOrdem())));
    }

    // Outros produtos da mesma atlética (até 4)
    public List<Produto> relacionados(Produto produto) {
        if (produto.getAtletica() == null) {
            return List.of();
        }
        return produtoRepository.buscarRelacionados(produto.getAtletica(), produto.getIdProduto(),
                PageRequest.of(0, 4));
    }

    // Últimos produtos cadastrados que têm estoque (tela inicial)
    public List<Produto> destaques() {
        return produtoRepository.buscarDestaques(PageRequest.of(0, 4));
    }

    // método para excluir um produto (bloqueia se ele já foi vendido)
    public void excluir(Integer id) {
        Produto produto = findById(id);
        if (produto == null) {
            throw new NegocioException("Produto não encontrado.");
        }
        long vendas = itemDoPedidoRepository.countByProduto(produto);
        if (vendas > 0) {
            throw new NegocioException("Não é possível excluir \"" + produto.getDescricaoProduto()
                    + "\": ele aparece em " + vendas + " pedido(s). Para tirá-lo da loja, deixe o estoque em 0.");
        }
        produtoRepository.delete(produto);
    }

    // Campos de texto pesquisados nos produtos
    private List<Expression<String>> camposDeTexto(Root<Produto> root) {
        Join<Produto, Atletica> atletica = root.join("atletica", JoinType.LEFT);
        return List.of(
                root.<String>get("descricaoProduto"),
                root.<String>get("marcaProduto"),
                root.<String>get("modeloProduto"),
                root.<String>get("categoriaProduto"),
                atletica.<String>get("nomeAtletica"));
    }

    // Ordenação escolhida na loja
    private Sort ordenacao(String ordem) {
        if ("nome".equals(ordem)) {
            return Sort.by(Sort.Order.asc("descricaoProduto"));
        }
        if ("menorPreco".equals(ordem)) {
            return Sort.by(Sort.Order.asc("valorProduto"), Sort.Order.asc("descricaoProduto"));
        }
        if ("maiorPreco".equals(ordem)) {
            return Sort.by(Sort.Order.desc("valorProduto"), Sort.Order.asc("descricaoProduto"));
        }
        return Sort.by(Sort.Order.desc("idProduto")); // mais recentes
    }
}
