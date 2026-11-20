package com.novosiga.novosiga.dto;

import org.springframework.web.util.UriComponentsBuilder;

import com.novosiga.novosiga.util.TextoUtil;

import lombok.Getter;
import lombok.Setter;

/**
 * Filtros da vitrine da loja (/loja). O Spring preenche os campos
 * automaticamente com os parâmetros da URL: /loja?q=camiseta&categoria=Camiseta
 */
@Getter
@Setter
public class FiltroLoja {

    public static final String ORDEM_PADRAO = "recentes";

    private String q;              // texto da busca
    private Integer atletica;      // id da atlética
    private String categoria;      // categoria do produto
    private Double precoMin;       // preço mínimo
    private Double precoMax;       // preço máximo
    private boolean disponiveis;   // somente produtos com estoque
    private String ordem = ORDEM_PADRAO; // recentes | nome | menorPreco | maiorPreco

    // Verdadeiro quando algum filtro foi aplicado (mostra o botão "Limpar filtros")
    public boolean isAtivo() {
        return !TextoUtil.vazio(q) || atletica != null || !TextoUtil.vazio(categoria)
                || precoMin != null || precoMax != null || disponiveis;
    }

    // URL com os filtros atuais (usada na paginação para não perder os filtros)
    public String getUrlBase() {
        UriComponentsBuilder url = UriComponentsBuilder.fromPath("/loja");
        if (!TextoUtil.vazio(q)) {
            url.queryParam("q", q);
        }
        if (atletica != null) {
            url.queryParam("atletica", atletica);
        }
        if (!TextoUtil.vazio(categoria)) {
            url.queryParam("categoria", categoria);
        }
        if (precoMin != null) {
            url.queryParam("precoMin", precoMin);
        }
        if (precoMax != null) {
            url.queryParam("precoMax", precoMax);
        }
        if (disponiveis) {
            url.queryParam("disponiveis", true);
        }
        if (ordem != null && !ORDEM_PADRAO.equals(ordem)) {
            url.queryParam("ordem", ordem);
        }
        return url.build().encode().toUriString();
    }

    // URL com os filtros atuais MENOS um (botão "x" das etiquetas de filtro)
    // campo: q | atletica | categoria | preco | disponiveis
    public String urlSem(String campo) {
        FiltroLoja copia = new FiltroLoja();
        copia.setQ(q);
        copia.setAtletica(atletica);
        copia.setCategoria(categoria);
        copia.setPrecoMin(precoMin);
        copia.setPrecoMax(precoMax);
        copia.setDisponiveis(disponiveis);
        copia.setOrdem(ordem);

        switch (campo) {
            case "q" -> copia.setQ(null);
            case "atletica" -> copia.setAtletica(null);
            case "categoria" -> copia.setCategoria(null);
            case "preco" -> {
                copia.setPrecoMin(null);
                copia.setPrecoMax(null);
            }
            case "disponiveis" -> copia.setDisponiveis(false);
            default -> {
                // campo desconhecido: mantém tudo
            }
        }
        return copia.getUrlBase();
    }
}
