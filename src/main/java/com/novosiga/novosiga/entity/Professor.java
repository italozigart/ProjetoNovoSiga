package com.novosiga.novosiga.entity;

import com.novosiga.novosiga.util.TextoUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class Professor {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idProfessor;

    @NotBlank(message = "Informe o nome do professor.")
    @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres.")
    @Column(nullable = false, length = 50)
    private String nomeProfessor;

    @NotBlank(message = "Informe o CPF.")
    @Pattern(regexp = "\\d{11}", message = "O CPF deve ter 11 números.")
    @Column(nullable = false, length = 11)
    private String cpfProfessor;

    @NotBlank(message = "Informe o RG.")
    @Size(max = 20, message = "O RG deve ter no máximo 20 caracteres.")
    @Column(nullable = false, length = 20)
    private String rgProfessor;

    @NotBlank(message = "Informe o telefone.")
    @Pattern(regexp = "\\d{10,11}", message = "Informe o telefone com DDD (10 ou 11 números).")
    @Column(nullable = false, length = 11)
    private String telefoneProfessor;

    @NotBlank(message = "Informe o endereço.")
    @Size(max = 40, message = "O endereço deve ter no máximo 40 caracteres.")
    @Column(nullable = false, length = 40)
    private String enderecoProfessor;

    @NotBlank(message = "Informe a graduação.")
    @Size(max = 50, message = "A graduação deve ter no máximo 50 caracteres.")
    @Column(nullable = false, length = 50)
    private String graduacaoProfessor;

    // Setters que limpam a máscara do CPF e do telefone
    public void setCpfProfessor(String cpfProfessor) {
        this.cpfProfessor = TextoUtil.somenteNumerosOuNulo(cpfProfessor);
    }

    public void setTelefoneProfessor(String telefoneProfessor) {
        this.telefoneProfessor = TextoUtil.somenteNumerosOuNulo(telefoneProfessor);
    }

    // Métodos usados nas telas
    public String getCpfFormatado() {
        return TextoUtil.formatarCpf(cpfProfessor);
    }

    public String getTelefoneFormatado() {
        return TextoUtil.formatarTelefone(telefoneProfessor);
    }
}
