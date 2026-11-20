package com.novosiga.novosiga.busca;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.springframework.data.jpa.domain.Specification;

import com.novosiga.novosiga.util.TextoUtil;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Monta as consultas usadas pelas barras de busca do sistema.
 *
 * Regras de encontrabilidade:
 *  - o texto é dividido em palavras: "camiseta preta" vira ["camiseta", "preta"];
 *  - cada palavra precisa aparecer em PELO MENOS UM dos campos pesquisados;
 *  - maiúsculas e acentos são ignorados: "joao" encontra "João", "calca" encontra "Calça";
 *  - em campos numéricos (CPF, telefone) a pontuação é ignorada: "123.456" encontra "12345678900".
 */
public final class Busca {

    // Letras acentuadas e a versão sem acento NA MESMA POSIÇÃO (usado pela função translate do banco)
    private static final String COM_ACENTO = "ÁÀÂÃÄáàâãäÉÈÊËéèêëÍÌÎÏíìîïÓÒÔÕÖóòôõöÚÙÛÜúùûüÇçÑñ";
    private static final String SEM_ACENTO = "AAAAAaaaaaEEEEeeeeIIIIiiiiOOOOOoooooUUUUuuuuCcNn";

    // Limite de palavras consideradas (evita consultas enormes)
    private static final int MAXIMO_PALAVRAS = 6;

    private Busca() {
    }

    // Divide o termo em palavras já normalizadas (minúsculas e sem acento)
    public static List<String> palavras(String termo) {
        String normalizado = TextoUtil.normalizar(termo);
        if (normalizado.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(normalizado.split(" "))
                .filter(palavra -> !palavra.isBlank())
                .distinct()
                .limit(MAXIMO_PALAVRAS)
                .toList();
    }

    // SQL gerado: lower(translate(campo, 'ÁÀ...', 'AA...'))
    public static Expression<String> semAcento(CriteriaBuilder cb, Expression<String> campo) {
        Expression<String> semAcentos = cb.function("translate", String.class,
                campo, cb.literal(COM_ACENTO), cb.literal(SEM_ACENTO));
        return cb.lower(semAcentos);
    }

    // campo LIKE '%palavra%' ignorando acentos e maiúsculas
    public static Predicate contem(CriteriaBuilder cb, Expression<String> campo, String palavraNormalizada) {
        return cb.like(semAcento(cb, campo), "%" + escaparLike(palavraNormalizada) + "%", '\\');
    }

    // A palavra precisa aparecer em algum dos campos de texto (ou, se tiver dígitos, nos campos numéricos)
    public static Predicate algumCampoContem(CriteriaBuilder cb, List<Expression<String>> camposTexto,
            List<Expression<String>> camposNumericos, String palavra) {

        List<Predicate> alternativas = new ArrayList<>();

        for (Expression<String> campo : camposTexto) {
            alternativas.add(contem(cb, campo, palavra));
        }

        String digitos = TextoUtil.somenteNumeros(palavra);
        if (digitos != null && !digitos.isEmpty()) {
            for (Expression<String> campo : camposNumericos) {
                alternativas.add(cb.like(campo, "%" + digitos + "%"));
            }
        }

        return cb.or(alternativas.toArray(new Predicate[0]));
    }

    /**
     * Cria a Specification usada no repository.findAll(spec, pageable).
     *
     * @param termo           texto digitado na barra de busca
     * @param camposTexto     campos de texto pesquisados (nome, descrição...)
     * @param camposNumericos campos só com números (CPF, telefone) ou null
     */
    public static <T> Specification<T> porTermo(String termo,
            Function<Root<T>, List<Expression<String>>> camposTexto,
            Function<Root<T>, List<Expression<String>>> camposNumericos) {

        return (root, query, cb) -> {
            List<String> palavras = palavras(termo);
            if (palavras.isEmpty()) {
                return cb.conjunction(); // sem termo = traz tudo
            }

            List<Expression<String>> textos = camposTexto.apply(root);
            List<Expression<String>> numeros = (camposNumericos == null) ? List.of() : camposNumericos.apply(root);

            List<Predicate> condicoes = new ArrayList<>();
            for (String palavra : palavras) {
                condicoes.add(algumCampoContem(cb, textos, numeros, palavra));
            }
            return cb.and(condicoes.toArray(new Predicate[0]));
        };
    }

    // Faz o % e o _ digitados serem tratados como texto comum no LIKE
    private static String escaparLike(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
