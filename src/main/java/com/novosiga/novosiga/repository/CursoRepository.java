package com.novosiga.novosiga.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.novosiga.novosiga.entity.Curso;

public interface CursoRepository extends JpaRepository<Curso, Integer>, JpaSpecificationExecutor<Curso> {

}
