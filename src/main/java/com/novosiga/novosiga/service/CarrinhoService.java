package com.novosiga.novosiga.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.novosiga.novosiga.carrinho.Carrinho;
import com.novosiga.novosiga.carrinho.ItemCarrinho;
import com.novosiga.novosiga.carrinho.ResumoCarrinho;
import com.novosiga.novosiga.entity.Produto;
import com.novosiga.novosiga.exception.NegocioException;
import com.novosiga.novosiga.repository.ProdutoRepository;

/**
 * Regras do carrinho de compras (o carrinho em si fica na sessão do usuário).
 */
@Service
public class CarrinhoService {

    @Autowired
    private Carrinho carrinho;

    @Autowired
    private ProdutoRepository produtoRepository;

    // Adiciona o produto conferindo o estoque disponível
    public Produto adicionar(Integer idProduto, int quantidade) {
        if (idProduto == null) {
            throw new NegocioException("Produto não informado.");
        }
        if (quantidade < 1) {
            throw new NegocioException("Informe uma quantidade maior que zero.");
        }

        Produto produto = produtoRepository.findById(idProduto)
                .orElseThrow(() -> new NegocioException("Produto não encontrado."));

        if (!produto.isDisponivel()) {
            throw new NegocioException("\"" + produto.getDescricaoProduto() + "\" está esgotado no momento.");
        }

        int jaNoCarrinho = carrinho.getQuantidade(idProduto);
        if (!produto.temEstoque(jaNoCarrinho + quantidade)) {
            String mensagem = "Temos apenas " + produto.getEstoqueDisponivel() + " unidade(s) de \""
                    + produto.getDescricaoProduto() + "\" em estoque";
            if (jaNoCarrinho > 0) {
                mensagem += " e você já tem " + jaNoCarrinho + " no carrinho";
            }
            throw new NegocioException(mensagem + ".");
        }

        carrinho.adicionar(idProduto, quantidade);
        return produto;
    }

    // Altera a quantidade (0 remove o item)
    public void alterarQuantidade(Integer idProduto, int quantidade) {
        if (quantidade <= 0) {
            carrinho.remover(idProduto);
            return;
        }

        Produto produto = produtoRepository.findById(idProduto)
                .orElseThrow(() -> new NegocioException("Produto não encontrado."));

        if (!produto.temEstoque(quantidade)) {
            throw new NegocioException("Temos apenas " + produto.getEstoqueDisponivel() + " unidade(s) de \""
                    + produto.getDescricaoProduto() + "\" em estoque.");
        }

        carrinho.alterarQuantidade(idProduto, quantidade);
    }

    public void remover(Integer idProduto) {
        carrinho.remover(idProduto);
    }

    public void limpar() {
        carrinho.limpar();
    }

    // idProduto -> quantidade (usado na finalização da compra)
    public Map<Integer, Integer> getItens() {
        return carrinho.getItens();
    }

    // Monta os itens com os dados atuais do banco (preço e estoque sempre atualizados)
    public ResumoCarrinho resumo() {
        Map<Integer, Integer> itens = carrinho.getItens();
        List<ItemCarrinho> lista = new ArrayList<>();

        if (!itens.isEmpty()) {
            Map<Integer, Produto> produtos = new HashMap<>();
            for (Produto produto : produtoRepository.findAllById(new ArrayList<>(itens.keySet()))) {
                produtos.put(produto.getIdProduto(), produto);
            }

            for (Map.Entry<Integer, Integer> entrada : itens.entrySet()) {
                Produto produto = produtos.get(entrada.getKey());
                if (produto == null) {
                    // o produto foi excluído depois de entrar no carrinho
                    carrinho.remover(entrada.getKey());
                } else {
                    lista.add(new ItemCarrinho(produto, entrada.getValue()));
                }
            }
        }

        return new ResumoCarrinho(lista);
    }
}
