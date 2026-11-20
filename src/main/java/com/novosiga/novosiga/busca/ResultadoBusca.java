package com.novosiga.novosiga.busca;

import org.springframework.data.domain.Page;

import com.novosiga.novosiga.entity.Aluno;
import com.novosiga.novosiga.entity.Atletica;
import com.novosiga.novosiga.entity.Curso;
import com.novosiga.novosiga.entity.Disciplina;
import com.novosiga.novosiga.entity.Pedido;
import com.novosiga.novosiga.entity.Produto;
import com.novosiga.novosiga.entity.Professor;

import lombok.Getter;
import lombok.Setter;

/**
 * Resultado da busca geral (barra de busca do cabeçalho), separado por tipo.
 * Cada grupo traz os primeiros resultados e o total encontrado.
 */
@Getter
@Setter
public class ResultadoBusca {

    private final String termo;

    private Page<Produto> produtos = Page.empty();
    private Page<Aluno> alunos = Page.empty();
    private Page<Professor> professores = Page.empty();
    private Page<Disciplina> disciplinas = Page.empty();
    private Page<Curso> cursos = Page.empty();
    private Page<Atletica> atleticas = Page.empty();
    private Page<Pedido> pedidos = Page.empty();

    public ResultadoBusca(String termo) {
        this.termo = termo;
    }

    // Total de registros encontrados em todos os grupos
    public long getTotal() {
        return produtos.getTotalElements() + alunos.getTotalElements() + professores.getTotalElements()
                + disciplinas.getTotalElements() + cursos.getTotalElements() + atleticas.getTotalElements()
                + pedidos.getTotalElements();
    }

    public boolean isVazio() {
        return getTotal() == 0;
    }
}
