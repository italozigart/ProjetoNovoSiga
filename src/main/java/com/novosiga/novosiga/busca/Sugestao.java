package com.novosiga.novosiga.busca;

/**
 * Sugestão exibida enquanto o usuário digita na busca do cabeçalho (autocompletar).
 * É devolvida em JSON pelo endpoint /busca/sugestoes.
 *
 * @param tipo    Aluno, Produto, Disciplina...
 * @param icone   classe do ícone Font Awesome (ex.: fa-user-graduate)
 * @param titulo  texto principal
 * @param detalhe texto secundário
 * @param url     página aberta ao clicar
 */
public record Sugestao(String tipo, String icone, String titulo, String detalhe, String url) {
}
