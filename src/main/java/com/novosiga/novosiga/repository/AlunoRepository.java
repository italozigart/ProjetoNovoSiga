package com.novosiga.novosiga.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.novosiga.novosiga.entity.Aluno;
import com.novosiga.novosiga.entity.Curso;

// JpaSpecificationExecutor permite montar buscas com filtros dinâmicos (barra de busca)
public interface AlunoRepository extends JpaRepository<Aluno, Integer>, JpaSpecificationExecutor<Aluno> {

    // Quantos alunos estão matriculados no curso (usado antes de excluir um curso)
    long countByCurso(Curso curso);
}
