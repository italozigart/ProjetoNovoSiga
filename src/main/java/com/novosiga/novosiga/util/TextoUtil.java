package com.novosiga.novosiga.util;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Métodos auxiliares para tratar textos digitados nos formulários e na busca.
 */
public final class TextoUtil {

    private TextoUtil() {
    }

    // Verdadeiro quando o texto é nulo, vazio ou só tem espaços
    public static boolean vazio(String texto) {
        return texto == null || texto.isBlank();
    }

    // Mantém apenas os dígitos. Ex.: "123.456.789-00" -> "12345678900"
    public static String somenteNumeros(String texto) {
        if (texto == null) {
            return null;
        }
        return texto.replaceAll("\\D", "");
    }

    // Igual ao somenteNumeros, mas devolve null quando não sobra nenhum dígito
    // (assim a validação @NotBlank mostra a mensagem "Informe o ...")
    public static String somenteNumerosOuNulo(String texto) {
        String numeros = somenteNumeros(texto);
        return (numeros == null || numeros.isEmpty()) ? null : numeros;
    }

    // Remove acentos, espaços extras e deixa minúsculo. Ex.: "  Calça  João " -> "calca joao"
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    // Deixa o texto em maiúsculas (ex.: sigla do estado "sp" -> "SP")
    public static String maiusculo(String texto) {
        return texto == null ? null : texto.trim().toUpperCase(Locale.ROOT);
    }

    // 12345678900 -> 123.456.789-00
    public static String formatarCpf(String cpf) {
        String n = somenteNumeros(cpf);
        if (n == null || n.length() != 11) {
            return cpf;
        }
        return n.substring(0, 3) + "." + n.substring(3, 6) + "." + n.substring(6, 9) + "-" + n.substring(9);
    }

    // 17999998888 -> (17) 99999-8888  |  1733334444 -> (17) 3333-4444
    public static String formatarTelefone(String telefone) {
        String n = somenteNumeros(telefone);
        if (n == null) {
            return null;
        }
        if (n.length() == 11) {
            return "(" + n.substring(0, 2) + ") " + n.substring(2, 7) + "-" + n.substring(7);
        }
        if (n.length() == 10) {
            return "(" + n.substring(0, 2) + ") " + n.substring(2, 6) + "-" + n.substring(6);
        }
        return telefone;
    }

    // 15700000 -> 15700-000
    public static String formatarCep(String cep) {
        String n = somenteNumeros(cep);
        if (n == null || n.length() != 8) {
            return cep;
        }
        return n.substring(0, 5) + "-" + n.substring(5);
    }

    // Arredonda valores em dinheiro para 2 casas (evita 59.900000000001)
    public static double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
