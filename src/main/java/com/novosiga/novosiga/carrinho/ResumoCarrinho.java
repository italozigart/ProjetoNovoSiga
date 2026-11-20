package com.novosiga.novosiga.carrinho;

import java.util.List;

import com.novosiga.novosiga.util.TextoUtil;

import lombok.Getter;

/**
 * Totais do carrinho usados nas telas do carrinho e de finalização.
 */
@Getter
public class ResumoCarrinho {

    private final List<ItemCarrinho> itens;
    private final double total;
    private final int totalUnidades;
    private final boolean problemaEstoque;

    public ResumoCarrinho(List<ItemCarrinho> itens) {
        this.itens = itens;

        double soma = 0;
        int unidades = 0;
        boolean faltaEstoque = false;

        for (ItemCarrinho item : itens) {
            soma += item.getSubtotal();
            unidades += item.getQuantidade();
            if (item.isEstoqueInsuficiente()) {
                faltaEstoque = true;
            }
        }

        this.total = TextoUtil.arredondar(soma);
        this.totalUnidades = unidades;
        this.problemaEstoque = faltaEstoque;
    }

    public boolean isVazio() {
        return itens.isEmpty();
    }
}
