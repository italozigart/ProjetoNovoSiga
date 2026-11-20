package com.novosiga.novosiga.carrinho;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

/**
 * Carrinho de compras do usuário.
 *
 * @SessionScope: o Spring cria UM carrinho para cada sessão (cada usuário logado
 * tem o seu). Ao fazer logout a sessão termina e o carrinho é descartado.
 *
 * Guardamos apenas o código do produto e a quantidade. Preço, nome e estoque
 * são sempre lidos do banco na hora de exibir/finalizar, assim ficam atualizados.
 */
@Component
@SessionScope
public class Carrinho implements Serializable {

    private static final long serialVersionUID = 1L;

    // idProduto -> quantidade (LinkedHashMap mantém a ordem em que foram adicionados)
    private final Map<Integer, Integer> itens = new LinkedHashMap<>();

    // Soma a quantidade ao produto (se já estiver no carrinho)
    public void adicionar(Integer idProduto, int quantidade) {
        if (idProduto == null || quantidade <= 0) {
            return;
        }
        itens.merge(idProduto, quantidade, Integer::sum);
    }

    // Define a quantidade do produto (0 ou menos remove)
    public void alterarQuantidade(Integer idProduto, int quantidade) {
        if (idProduto == null) {
            return;
        }
        if (quantidade <= 0) {
            itens.remove(idProduto);
        } else {
            itens.put(idProduto, quantidade);
        }
    }

    public void remover(Integer idProduto) {
        itens.remove(idProduto);
    }

    public void limpar() {
        itens.clear();
    }

    // Quantidade de um produto no carrinho (0 se não estiver)
    public int getQuantidade(Integer idProduto) {
        return itens.getOrDefault(idProduto, 0);
    }

    // Cópia somente leitura dos itens
    public Map<Integer, Integer> getItens() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(itens));
    }

    // Soma das quantidades (número exibido no ícone do carrinho)
    public int getTotalUnidades() {
        int total = 0;
        for (Integer quantidade : itens.values()) {
            total += quantidade;
        }
        return total;
    }

    public boolean isVazio() {
        return itens.isEmpty();
    }
}
