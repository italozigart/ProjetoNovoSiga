package com.novosiga.novosiga.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.novosiga.novosiga.busca.Busca;
import com.novosiga.novosiga.dto.ItemPedidoRequest;
import com.novosiga.novosiga.dto.PedidoRequest;
import com.novosiga.novosiga.entity.Aluno;
import com.novosiga.novosiga.entity.ItemDoPedido;
import com.novosiga.novosiga.entity.Pedido;
import com.novosiga.novosiga.entity.Produto;
import com.novosiga.novosiga.exception.NegocioException;
import com.novosiga.novosiga.repository.AlunoRepository;
import com.novosiga.novosiga.repository.PedidoRepository;
import com.novosiga.novosiga.repository.ProdutoRepository;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

@Service
public class PedidoService {

    // Formas de pagamento aceitas
    public static final List<String> FORMAS_PAGAMENTO = List.of(
            "PIX", "Cartão de crédito", "Cartão de débito", "Dinheiro");

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    // Finaliza a compra do carrinho (idProduto -> quantidade)
    @Transactional
    public Pedido finalizarCompra(Integer idAluno, String formaPagamento, Map<Integer, Integer> itensCarrinho) {
        return criarPedido(idAluno, formaPagamento, itensCarrinho);
    }

    // Pedido feito pela tela "Novo pedido (balcão)" (JSON enviado pelo fetch)
    @Transactional
    public Pedido salvarPedido(PedidoRequest request) {
        if (request == null) {
            throw new NegocioException("Dados do pedido não foram enviados.");
        }
        // Junta itens repetidos do mesmo produto
        Map<Integer, Integer> itens = new LinkedHashMap<>();
        if (request.itens() != null) {
            for (ItemPedidoRequest item : request.itens()) {
                if (item == null || item.idProduto() == null || item.quantidade() == null || item.quantidade() < 1) {
                    throw new NegocioException("Há um item do pedido com produto ou quantidade inválida.");
                }
                itens.merge(item.idProduto(), item.quantidade(), Integer::sum);
            }
        }
        return criarPedido(request.idAluno(), request.formaPagamento(), itens);
    }

    /*
     * Regra principal da venda:
     * 1. valida aluno, forma de pagamento e itens;
     * 2. para cada produto: trava a linha no banco, confere o estoque e dá baixa;
     * 3. grava o pedido com os itens e o total.
     * Como o método roda dentro de @Transactional, se QUALQUER produto não tiver
     * estoque nada é gravado (rollback) e a mensagem volta para a tela.
     */
    private Pedido criarPedido(Integer idAluno, String formaPagamento, Map<Integer, Integer> itens) {

        if (idAluno == null) {
            throw new NegocioException("Selecione o aluno que está comprando.");
        }
        Aluno aluno = alunoRepository.findById(idAluno)
                .orElseThrow(() -> new NegocioException("Aluno não encontrado."));

        if (formaPagamento == null || !FORMAS_PAGAMENTO.contains(formaPagamento)) {
            throw new NegocioException("Escolha a forma de pagamento.");
        }

        if (itens == null || itens.isEmpty()) {
            throw new NegocioException("Adicione pelo menos um produto ao pedido.");
        }

        Pedido pedido = new Pedido();
        pedido.setAluno(aluno);
        pedido.setDataPedido(LocalDate.now());
        pedido.setFormaPagamento(formaPagamento);

        for (Map.Entry<Integer, Integer> entrada : itens.entrySet()) {
            Integer idProduto = entrada.getKey();
            Integer quantidade = entrada.getValue();

            if (quantidade == null || quantidade < 1) {
                throw new NegocioException("Quantidade inválida no pedido.");
            }

            // Busca o produto travando a linha (evita vender o mesmo item duas vezes)
            Produto produto = produtoRepository.buscarParaAtualizarEstoque(idProduto)
                    .orElseThrow(() -> new NegocioException("Produto não encontrado (código " + idProduto + ")."));

            // Confere e baixa o estoque (lança NegocioException se faltar)
            produto.baixarEstoque(quantidade);

            ItemDoPedido item = new ItemDoPedido();
            item.setProduto(produto);
            item.setQuantidade(quantidade);
            item.setPreco(produto.getValorProduto()); // preço do momento da compra
            item.atualizarSubtotal();
            item.setPedido(pedido);
            pedido.getItens().add(item);
        }

        // Calcula o valor total do pedido
        pedido.atualizarTotal();

        // Salva o pedido e seus itens (cascade = ALL)
        return pedidoRepository.save(pedido);
    }

    public Pedido findById(Integer id) {
        return pedidoRepository.findById(id).orElse(null);
    }

    public long contar() {
        return pedidoRepository.count();
    }

    // Busca paginada: número do pedido (12 ou #12), nome/CPF do aluno ou forma de pagamento
    public Page<Pedido> buscar(String termo, int pagina, int tamanho) {
        Specification<Pedido> spec = (root, query, cb) -> {
            List<String> palavras = Busca.palavras(termo);
            if (palavras.isEmpty()) {
                return cb.conjunction();
            }

            Join<Pedido, Aluno> aluno = root.join("aluno", JoinType.LEFT);
            List<Expression<String>> textos = List.of(aluno.<String>get("nomeAluno"), root.<String>get("formaPagamento"));
            List<Expression<String>> numeros = List.of(aluno.<String>get("cpfAluno"));

            List<Predicate> condicoes = new ArrayList<>();
            for (String palavra : palavras) {
                Predicate condicao = Busca.algumCampoContem(cb, textos, numeros, palavra);
                String numero = palavra.startsWith("#") ? palavra.substring(1) : palavra;
                if (numero.matches("\\d{1,9}")) {
                    condicao = cb.or(condicao, cb.equal(root.get("idPedido"), Integer.valueOf(numero)));
                }
                condicoes.add(condicao);
            }
            return cb.and(condicoes.toArray(new Predicate[0]));
        };

        return pedidoRepository.findAll(spec,
                PageRequest.of(Math.max(pagina, 0), tamanho, Sort.by(Sort.Order.desc("idPedido"))));
    }
}
