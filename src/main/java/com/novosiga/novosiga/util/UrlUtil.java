package com.novosiga.novosiga.util;

import org.springframework.web.util.UriComponentsBuilder;

/**
 * Montagem de URLs (paginação) e redirecionamentos seguros.
 */
public final class UrlUtil {

    private UrlUtil() {
    }

    // Ex.: comParametro("/alunos/listar", "q", "joão") -> "/alunos/listar?q=jo%C3%A3o"
    public static String comParametro(String caminho, String nome, String valor) {
        UriComponentsBuilder url = UriComponentsBuilder.fromPath(caminho);
        if (!TextoUtil.vazio(valor)) {
            url.queryParam(nome, valor);
        }
        return url.build().encode().toUriString();
    }

    // Só aceita endereços do próprio sistema ("/loja?..."), nunca sites externos
    public static boolean enderecoLocal(String destino) {
        return destino != null
                && destino.startsWith("/")
                && !destino.startsWith("//")
                && !destino.contains("\\")
                && !destino.contains("\r")
                && !destino.contains("\n");
    }

    // "redirect:" para o destino informado, ou para o padrão se o destino não for local
    public static String redirecionar(String destino, String padrao) {
        return "redirect:" + (enderecoLocal(destino) ? destino : padrao);
    }
}
