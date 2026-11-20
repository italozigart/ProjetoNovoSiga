package com.novosiga.novosiga.carrinho;

import com.novosiga.novosiga.entity.Produto;
import com.novosiga.novosiga.util.TextoUtil;

import lombok.Getter;

/**
 * Item exibido na tela do carrinho: o produto (com dados atuais do banco) e a quantidade.
 */
@Getter
public class ItemCarrinho {

    private final Produto produto;
    private final int quantidade;

    public ItemCarrinho(Produto produto, int quantidade) {
        this.produto = produto;
        this.quantidade = quantidade;
    }

    public double getSubtotal() {
        return TextoUtil.arredondar(produto.getValorProduto() * quantidade);
    }

    // Verdadeiro quando a quantidade no carrinho é maior que o estoque atual
    public boolean isEstoqueInsuficiente() {
        return !produto.temEstoque(quantidade);
    }
}
