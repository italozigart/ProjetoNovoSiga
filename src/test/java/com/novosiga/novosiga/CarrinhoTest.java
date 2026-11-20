package com.novosiga.novosiga;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.novosiga.novosiga.carrinho.Carrinho;

/**
 * Testes do carrinho (não precisam de banco de dados).
 */
class CarrinhoTest {

    @Test
    void adicionarOMesmoProdutoSomaAsQuantidades() {
        Carrinho carrinho = new Carrinho();
        carrinho.adicionar(1, 2);
        carrinho.adicionar(1, 3);
        carrinho.adicionar(2, 1);

        assertThat(carrinho.getQuantidade(1)).isEqualTo(5);
        assertThat(carrinho.getTotalUnidades()).isEqualTo(6);
        assertThat(carrinho.getItens()).hasSize(2);
    }

    @Test
    void alterarParaZeroRemoveOItem() {
        Carrinho carrinho = new Carrinho();
        carrinho.adicionar(1, 2);
        carrinho.alterarQuantidade(1, 0);

        assertThat(carrinho.isVazio()).isTrue();
    }

    @Test
    void limparEsvaziaOCarrinho() {
        Carrinho carrinho = new Carrinho();
        carrinho.adicionar(1, 2);
        carrinho.adicionar(3, 1);
        carrinho.limpar();

        assertThat(carrinho.getTotalUnidades()).isZero();
        assertThat(carrinho.isVazio()).isTrue();
    }

    @Test
    void quantidadeInvalidaNaoEntraNoCarrinho() {
        Carrinho carrinho = new Carrinho();
        carrinho.adicionar(1, 0);
        carrinho.adicionar(null, 2);

        assertThat(carrinho.isVazio()).isTrue();
    }
}
