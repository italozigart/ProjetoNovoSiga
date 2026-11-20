package com.novosiga.novosiga.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.novosiga.novosiga.entity.Aluno;
import com.novosiga.novosiga.entity.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Integer>, JpaSpecificationExecutor<Pedido> {

    long countByAluno(Aluno aluno);
}
