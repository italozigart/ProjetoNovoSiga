package com.novosiga.novosiga.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.novosiga.novosiga.busca.Busca;
import com.novosiga.novosiga.entity.Curso;
import com.novosiga.novosiga.exception.NegocioException;
import com.novosiga.novosiga.repository.AlunoRepository;
import com.novosiga.novosiga.repository.AtleticaRepository;
import com.novosiga.novosiga.repository.CursoRepository;
import com.novosiga.novosiga.repository.DisciplinaRepository;

@Service
public class CursoService {

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    @Autowired
    private AtleticaRepository atleticaRepository;

    public Curso save(Curso curso) {
        return cursoRepository.save(curso);
    }

    // Lista em ordem alfabética (usada nos selects dos formulários)
    public List<Curso> findAll() {
        return cursoRepository.findAll(Sort.by("nomeCurso"));
    }

    public Curso findById(Integer id) {
        return cursoRepository.findById(id).orElse(null);
    }

    public long contar() {
        return cursoRepository.count();
    }

    // Busca paginada por nome ou descrição
    public Page<Curso> buscar(String termo, int pagina, int tamanho) {
        Specification<Curso> spec = Busca.porTermo(termo,
                root -> List.of(root.<String>get("nomeCurso"), root.<String>get("descricaoCurso")),
                null);
        return cursoRepository.findAll(spec, PageRequest.of(Math.max(pagina, 0), tamanho, Sort.by("nomeCurso")));
    }

    // Exclui o curso somente se não houver alunos, disciplinas ou atléticas ligados a ele
    public void excluir(Integer id) {
        Curso curso = findById(id);
        if (curso == null) {
            throw new NegocioException("Curso não encontrado.");
        }

        List<String> vinculos = new ArrayList<>();
        long alunos = alunoRepository.countByCurso(curso);
        long disciplinas = disciplinaRepository.countByCurso(curso);
        long atleticas = atleticaRepository.countByCurso(curso);
        if (alunos > 0) {
            vinculos.add(alunos + " aluno(s)");
        }
        if (disciplinas > 0) {
            vinculos.add(disciplinas + " disciplina(s)");
        }
        if (atleticas > 0) {
            vinculos.add(atleticas + " atlética(s)");
        }

        if (!vinculos.isEmpty()) {
            throw new NegocioException("Não é possível excluir o curso \"" + curso.getNomeCurso()
                    + "\": ele possui " + String.join(", ", vinculos) + " vinculado(s).");
        }

        cursoRepository.delete(curso);
    }
}
