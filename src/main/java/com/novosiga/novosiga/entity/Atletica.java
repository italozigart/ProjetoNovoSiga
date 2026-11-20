package com.novosiga.novosiga.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Associação Atlética que vende os produtos da loja (camisetas, canecas...).
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Atletica {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idAtletica;

    @NotBlank(message = "Informe o nome da atlética.")
    @Size(max = 60, message = "O nome deve ter no máximo 60 caracteres.")
    @Column(nullable = false, length = 60)
    private String nomeAtletica;

    @Size(max = 200, message = "A descrição deve ter no máximo 200 caracteres.")
    @Column(length = 200)
    private String descricaoAtletica;

    // Usuário do Instagram, sem o @ (ex.: atletica.si)
    @Pattern(regexp = "[A-Za-z0-9._]{1,30}", message = "Use apenas letras, números, ponto e _ (sem espaços).")
    @Column(length = 30)
    private String instagramAtletica;

    // Curso que a atlética representa (opcional: pode ser uma atlética geral)
    @ManyToOne
    @JoinColumn(name = "id_curso_fk")
    private Curso curso;

    @Lob
    private byte[] logoAtletica;

    @Column(length = 20)
    private String tipoLogo;

    // Uma atlética vende vários produtos
    @OneToMany(mappedBy = "atletica")
    private List<Produto> produtos;

    // Aceita "@atletica.si" ou "atletica.si" e grava "atletica.si"
    public void setInstagramAtletica(String instagramAtletica) {
        if (instagramAtletica == null) {
            this.instagramAtletica = null;
            return;
        }
        String usuario = instagramAtletica.trim().replace("@", "");
        this.instagramAtletica = usuario.isEmpty() ? null : usuario;
    }

    public boolean isTemLogo() {
        return logoAtletica != null && logoAtletica.length > 0;
    }

    // Quantidade de produtos cadastrados para a atlética
    public int getQuantidadeProdutos() {
        return produtos == null ? 0 : produtos.size();
    }
}
