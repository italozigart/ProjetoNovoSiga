package com.novosiga.novosiga;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.novosiga.novosiga.busca.Busca;
import com.novosiga.novosiga.util.TextoUtil;
import com.novosiga.novosiga.util.UrlUtil;

/**
 * Testes das funções de texto usadas na busca e nos formulários.
 */
class TextoUtilTest {

    @Test
    void normalizarRemoveAcentosEspacosEMaiusculas() {
        assertThat(TextoUtil.normalizar("  Calça   JOÃO ")).isEqualTo("calca joao");
    }

    @Test
    void buscaDivideOTermoEmPalavras() {
        assertThat(Busca.palavras("Camiseta  Preta camiseta")).containsExactly("camiseta", "preta");
        assertThat(Busca.palavras("   ")).isEmpty();
        assertThat(Busca.palavras(null)).isEmpty();
    }

    @Test
    void somenteNumerosLimpaAMascara() {
        assertThat(TextoUtil.somenteNumeros("123.456.789-00")).isEqualTo("12345678900");
        assertThat(TextoUtil.somenteNumerosOuNulo("abc")).isNull();
    }

    @Test
    void formatacoesParaAsTelas() {
        assertThat(TextoUtil.formatarCpf("12345678900")).isEqualTo("123.456.789-00");
        assertThat(TextoUtil.formatarTelefone("17999998888")).isEqualTo("(17) 99999-8888");
        assertThat(TextoUtil.formatarTelefone("1733334444")).isEqualTo("(17) 3333-4444");
        assertThat(TextoUtil.formatarCep("15700000")).isEqualTo("15700-000");
        assertThat(TextoUtil.arredondar(0.1 + 0.2)).isEqualTo(0.3);
    }

    @Test
    void redirecionamentoSoAceitaEnderecosDoSistema() {
        assertThat(UrlUtil.redirecionar("/loja?categoria=Camiseta", "/carrinho")).isEqualTo("redirect:/loja?categoria=Camiseta");
        assertThat(UrlUtil.redirecionar("https://site-externo.com", "/carrinho")).isEqualTo("redirect:/carrinho");
        assertThat(UrlUtil.redirecionar("//site-externo.com", "/carrinho")).isEqualTo("redirect:/carrinho");
        assertThat(UrlUtil.redirecionar(null, "/carrinho")).isEqualTo("redirect:/carrinho");
    }
}
