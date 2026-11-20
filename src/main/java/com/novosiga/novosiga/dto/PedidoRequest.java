package com.novosiga.novosiga.dto;

import java.util.List;

/**
 * JSON enviado pela tela "Novo pedido (balcão)" via fetch:
 * { "idAluno": 1, "formaPagamento": "PIX", "itens": [ { "idProduto": 2, "quantidade": 3 } ] }
 */
public record PedidoRequest(Integer idAluno, String formaPagamento, List<ItemPedidoRequest> itens) {
}
