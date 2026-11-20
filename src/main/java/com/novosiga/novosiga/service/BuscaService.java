package com.novosiga.novosiga.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.novosiga.novosiga.busca.Busca;
import com.novosiga.novosiga.busca.ResultadoBusca;
import com.novosiga.novosiga.busca.Sugestao;
import com.novosiga.novosiga.entity.Aluno;
import com.novosiga.novosiga.entity.Atletica;
import com.novosiga.novosiga.entity.Curso;
import com.novosiga.novosiga.entity.Disciplina;
import com.novosiga.novosiga.entity.Pedido;
import com.novosiga.novosiga.entity.Produto;
import com.novosiga.novosiga.entity.Professor;
import com.novosiga.novosiga.util.TextoUtil;

/**
 * Busca geral do sistema (barra de busca do cabeçalho).
 * Reaproveita a busca de cada service e junta os resultados.
 */
@Service
public class BuscaService {

    // Quantos itens de cada tipo aparecem na página de resultados
    private static final int RESULTADOS_POR_GRUPO = 5;

    // Quantas sugestões aparecem enquanto o usuário digita
    private static final int MAXIMO_SUGESTOES = 8;

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private AlunoService alunoService;

    @Autowired
    private ProfessorService professorService;

    @Autowired
    private DisciplinaService disciplinaService;

    @Autowired
    private CursoService cursoService;

    @Autowired
    private AtleticaService atleticaService;

    @Autowired
    private PedidoService pedidoService;

    // Página de resultados (/busca?q=...)
    public ResultadoBusca buscar(String termo) {
        ResultadoBusca resultado = new ResultadoBusca(termo);
        if (Busca.palavras(termo).isEmpty()) {
            return resultado;
        }
        resultado.setProdutos(produtoService.buscar(termo, 0, RESULTADOS_POR_GRUPO));
        resultado.setAlunos(alunoService.buscar(termo, 0, RESULTADOS_POR_GRUPO));
        resultado.setProfessores(professorService.buscar(termo, 0, RESULTADOS_POR_GRUPO));
        resultado.setDisciplinas(disciplinaService.buscar(termo, 0, RESULTADOS_POR_GRUPO));
        resultado.setCursos(cursoService.buscar(termo, 0, RESULTADOS_POR_GRUPO));
        resultado.setAtleticas(atleticaService.buscar(termo, 0, RESULTADOS_POR_GRUPO));
        resultado.setPedidos(pedidoService.buscar(termo, 0, RESULTADOS_POR_GRUPO));
        return resultado;
    }

    // Sugestões do autocompletar (/busca/sugestoes?q=...) - a partir de 2 letras
    public List<Sugestao> sugerir(String termo) {
        List<Sugestao> sugestoes = new ArrayList<>();
        if (TextoUtil.normalizar(termo).length() < 2) {
            return sugestoes;
        }

        for (Produto p : produtoService.buscar(termo, 0, 3).getContent()) {
            String atletica = p.getAtletica() == null ? "Produto" : p.getAtletica().getNomeAtletica();
            sugestoes.add(new Sugestao("Produto", "fa-bag-shopping", p.getDescricaoProduto(),
                    atletica + " • R$ " + String.format(PT_BR, "%.2f", p.getValorProduto()),
                    "/loja/produto/" + p.getIdProduto()));
        }
        for (Aluno a : alunoService.buscar(termo, 0, 2).getContent()) {
            String curso = a.getCurso() == null ? "Aluno" : a.getCurso().getNomeCurso();
            sugestoes.add(new Sugestao("Aluno", "fa-user-graduate", a.getNomeAluno(), curso,
                    "/alunos/editar/" + a.getIdAluno()));
        }
        for (Professor p : professorService.buscar(termo, 0, 2).getContent()) {
            sugestoes.add(new Sugestao("Professor", "fa-chalkboard-user", p.getNomeProfessor(),
                    p.getGraduacaoProfessor(), "/professores/editar/" + p.getIdProfessor()));
        }
        for (Disciplina d : disciplinaService.buscar(termo, 0, 2).getContent()) {
            String curso = d.getCurso() == null ? "" : " • " + d.getCurso().getNomeCurso();
            sugestoes.add(new Sugestao("Disciplina", "fa-book", d.getNomeDisciplina(),
                    d.getSiglaDisciplina() + curso, "/disciplinas/editar/" + d.getIdDisciplina()));
        }
        for (Curso c : cursoService.buscar(termo, 0, 2).getContent()) {
            sugestoes.add(new Sugestao("Curso", "fa-book-open", c.getNomeCurso(),
                    c.getCargaHoraria() + " horas", "/cursos/editar/" + c.getIdCurso()));
        }
        for (Atletica a : atleticaService.buscar(termo, 0, 2).getContent()) {
            sugestoes.add(new Sugestao("Atlética", "fa-trophy", a.getNomeAtletica(),
                    "Ver produtos na loja", "/loja?atletica=" + a.getIdAtletica()));
        }
        for (Pedido p : pedidoService.buscar(termo, 0, 1).getContent()) {
            String aluno = p.getAluno() == null ? "" : p.getAluno().getNomeAluno();
            sugestoes.add(new Sugestao("Pedido", "fa-receipt", "Pedido #" + p.getIdPedido(), aluno,
                    "/pedidos/detalhes/" + p.getIdPedido()));
        }

        return sugestoes.size() > MAXIMO_SUGESTOES ? new ArrayList<>(sugestoes.subList(0, MAXIMO_SUGESTOES)) : sugestoes;
    }
}
