package com.novosiga.novosiga.entity;

import com.novosiga.novosiga.util.TextoUtil;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ItemDoPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idItem;

    private Integer quantidade;

    private Double preco;

    private Double subtotal;

    //relacionamento com pedido
    @ManyToOne
    @JoinColumn(name = "idPedido_fk")
    private Pedido pedido;

    //relacionamento com produto
    @ManyToOne
    @JoinColumn(name = "idProduto_fk")
    private Produto produto;

    //Método para calcular o subtotal (arredondado para 2 casas)
    public Double calcularSubtotal(){
        return TextoUtil.arredondar(quantidade * preco);
    }

    //Método para atualizar o subtotal
    public void atualizarSubtotal(){
        this.subtotal = calcularSubtotal();
    }

}
