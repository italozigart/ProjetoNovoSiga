package com.novosiga.novosiga.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.novosiga.novosiga.entity.Curso;
import com.novosiga.novosiga.entity.Disciplina;
import com.novosiga.novosiga.entity.Professor;

public interface DisciplinaRepository extends JpaRepository<Disciplina, Integer>, JpaSpecificationExecutor<Disciplina> {

    long countByCurso(Curso curso);

    long countByProfessor(Professor professor);
}
