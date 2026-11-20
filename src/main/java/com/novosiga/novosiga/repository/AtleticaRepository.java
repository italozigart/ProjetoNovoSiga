package com.novosiga.novosiga.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.novosiga.novosiga.entity.Atletica;
import com.novosiga.novosiga.entity.Curso;

public interface AtleticaRepository extends JpaRepository<Atletica, Integer>, JpaSpecificationExecutor<Atletica> {

    long countByCurso(Curso curso);
}
