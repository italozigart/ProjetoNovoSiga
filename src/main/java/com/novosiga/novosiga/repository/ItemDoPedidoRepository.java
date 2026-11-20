package com.novosiga.novosiga.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.novosiga.novosiga.entity.ItemDoPedido;
import com.novosiga.novosiga.entity.Produto;

public interface ItemDoPedidoRepository extends JpaRepository<ItemDoPedido, Integer>{

    // Em quantos itens de pedido o produto aparece (usado antes de excluir um produto)
    long countByProduto(Produto produto);
}
