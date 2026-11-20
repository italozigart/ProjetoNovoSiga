package com.novosiga.novosiga.entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.novosiga.novosiga.util.TextoUtil;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idPedido;

    private LocalDate dataPedido;

    private Double totalPedido;

    // Forma de pagamento escolhida (PIX, Cartão de crédito...)
    @Column(length = 30)
    private String formaPagamento;

    // Relacionamento com o aluno
    @ManyToOne
    @JoinColumn(name = "IdAluno_fk")
    private Aluno aluno;

    // Relacionamento com os itens do pedido
    @OneToMany(
        mappedBy = "pedido",
        cascade = CascadeType.ALL
    )
    @OrderBy("idItem")
    private List<ItemDoPedido> itens = new ArrayList<>();

    //método para calcular total
    public Double calcularTotal(){
        double total = 0.0;
        if(itens != null){
            for(ItemDoPedido item : itens){
                if (item.getSubtotal() != null) {
                    total += item.getSubtotal();
                }
            }
        }
        return TextoUtil.arredondar(total);
    }

    //método para atualizar o total
    public void atualizarTotal(){
        this.totalPedido = calcularTotal();
    }

    // Data no formato dd/MM/aaaa (usado nas telas)
    public String getDataFormatada() {
        return dataPedido == null ? "—" : dataPedido.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    // Soma das quantidades de todos os itens (usado nas telas)
    public int getQuantidadeItens() {
        int quantidade = 0;
        if (itens != null) {
            for (ItemDoPedido item : itens) {
                if (item.getQuantidade() != null) {
                    quantidade += item.getQuantidade();
                }
            }
        }
        return quantidade;
    }
}
