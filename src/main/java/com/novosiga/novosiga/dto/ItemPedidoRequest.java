package com.novosiga.novosiga.dto;

/**
 * Item do JSON enviado pela tela "Novo pedido (balcão)".
 */
public record ItemPedidoRequest(Integer idProduto, Integer quantidade) {
}
