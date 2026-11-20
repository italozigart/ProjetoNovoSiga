package com.novosiga.novosiga.entity;

import java.util.List;

import com.novosiga.novosiga.exception.NegocioException;
import com.novosiga.novosiga.util.TextoUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idProduto;

    @NotBlank(message = "Informe a descrição do produto.")
    @Size(max = 100, message = "A descrição deve ter no máximo 100 caracteres.")
    @Column(nullable = false, length = 100)
    private String descricaoProduto;

    @NotBlank(message = "Informe a marca.")
    @Size(max = 50, message = "A marca deve ter no máximo 50 caracteres.")
    @Column(nullable = false, length = 50)
    private String marcaProduto;

    @NotBlank(message = "Informe o modelo.")
    @Size(max = 50, message = "O modelo deve ter no máximo 50 caracteres.")
    @Column(nullable = false, length = 50)
    private String modeloProduto;

    @NotNull(message = "Informe o valor.")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero.")
    @Column(nullable = false)
    private Double valorProduto;

    // Categoria do produto (Camiseta, Moletom, Caneca, Copo...)
    @NotBlank(message = "Informe a categoria.")
    @Size(max = 30, message = "A categoria deve ter no máximo 30 caracteres.")
    @Column(length = 30)
    private String categoriaProduto;

    // Quantidade disponível para venda.
    // Produtos cadastrados antes desta versão ficam com 0 (default do banco).
    @NotNull(message = "Informe a quantidade em estoque.")
    @Min(value = 0, message = "O estoque não pode ser negativo.")
    @Column(columnDefinition = "integer default 0")
    private Integer estoqueProduto = 0;

    // Atlética que vende o produto
    @NotNull(message = "Selecione a atlética.")
    @ManyToOne
    @JoinColumn(name = "id_atletica_fk")
    private Atletica atletica;

    @Lob
    private byte[] imagemProduto;

    @Column(length = 20)
    private String tipoImagem;

    //um produto pode aparecer em vários itens de pedidos
    //isso representa o lado 1
    @OneToMany(mappedBy = "produto")
    private List<ItemDoPedido> itens;

    // ======================================================
    // MÉTODOS DE ESTOQUE
    // ======================================================

    // Estoque sem nulo (produtos antigos podem estar sem valor)
    public int getEstoqueDisponivel() {
        return estoqueProduto == null ? 0 : estoqueProduto;
    }

    public boolean isDisponivel() {
        return getEstoqueDisponivel() > 0;
    }

    public boolean temEstoque(int quantidade) {
        return quantidade <= getEstoqueDisponivel();
    }

    // Retira a quantidade vendida do estoque
    public void baixarEstoque(int quantidade) {
        if (!temEstoque(quantidade)) {
            throw new NegocioException("Estoque insuficiente para \"" + descricaoProduto
                    + "\". Disponível: " + getEstoqueDisponivel() + ".");
        }
        this.estoqueProduto = getEstoqueDisponivel() - quantidade;
    }

    // ======================================================
    // MÉTODOS USADOS NAS TELAS
    // ======================================================

    public boolean isTemImagem() {
        return imagemProduto != null && imagemProduto.length > 0;
    }

    // Ícone (Font Awesome) exibido quando o produto não tem imagem
    public String getIconeCategoria() {
        String c = TextoUtil.normalizar(categoriaProduto);
        if (c.contains("camis") || c.contains("regata") || c.contains("moletom") || c.contains("blusa")
                || c.contains("jaqueta") || c.contains("uniforme") || c.contains("abada")) {
            return "fa-shirt";
        }
        if (c.contains("caneca") || c.contains("xicara")) {
            return "fa-mug-hot";
        }
        if (c.contains("copo") || c.contains("garrafa") || c.contains("squeeze") || c.contains("tirante")) {
            return "fa-glass-water";
        }
        if (c.contains("bone") || c.contains("chapeu") || c.contains("viseira")) {
            return "fa-hat-cowboy";
        }
        if (c.contains("ingresso") || c.contains("festa") || c.contains("evento")) {
            return "fa-ticket";
        }
        if (c.contains("chaveiro")) {
            return "fa-key";
        }
        if (c.contains("adesivo")) {
            return "fa-note-sticky";
        }
        if (c.contains("bolsa") || c.contains("mochila") || c.contains("sacola") || c.contains("ecobag")) {
            return "fa-bag-shopping";
        }
        if (c.contains("meia")) {
            return "fa-socks";
        }
        return "fa-tag";
    }
}
