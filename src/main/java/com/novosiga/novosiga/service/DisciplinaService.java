package com.novosiga.novosiga.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.novosiga.novosiga.busca.Busca;
import com.novosiga.novosiga.entity.Curso;
import com.novosiga.novosiga.entity.Disciplina;
import com.novosiga.novosiga.entity.Professor;
import com.novosiga.novosiga.exception.NegocioException;
import com.novosiga.novosiga.repository.DisciplinaRepository;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

@Service
public class DisciplinaService {

    // injeção de dependência do repositório de disciplinas
    @Autowired
    private DisciplinaRepository disciplinaRepository;

    // método para salvar uma disciplina
    public Disciplina save(Disciplina disciplina) {
        return disciplinaRepository.save(disciplina);
    }

    // método para listar todas as disciplinas (ordem alfabética)
    public List<Disciplina> findAll() {
        return disciplinaRepository.findAll(Sort.by("nomeDisciplina"));
    }

    // método para buscar disciplina pelo ID
    public Disciplina findById(Integer id) {
        return disciplinaRepository.findById(id).orElse(null);
    }

    public long contar() {
        return disciplinaRepository.count();
    }

    // Busca paginada por nome, sigla, curso ou professor
    public Page<Disciplina> buscar(String termo, int pagina, int tamanho) {
        Specification<Disciplina> spec = Busca.porTermo(termo,
                root -> {
                    Join<Disciplina, Curso> curso = root.join("curso", JoinType.LEFT);
                    Join<Disciplina, Professor> professor = root.join("professor", JoinType.LEFT);
                    return List.of(
                            root.<String>get("nomeDisciplina"),
                            root.<String>get("siglaDisciplina"),
                            curso.<String>get("nomeCurso"),
                            professor.<String>get("nomeProfessor"));
                },
                null);

        return disciplinaRepository.findAll(spec,
                PageRequest.of(Math.max(pagina, 0), tamanho, Sort.by("nomeDisciplina")));
    }

    // método para excluir uma disciplina pelo ID
    public void excluir(Integer id) {
        Disciplina disciplina = findById(id);
        if (disciplina == null) {
            throw new NegocioException("Disciplina não encontrada.");
        }
        disciplinaRepository.delete(disciplina);
    }
}
