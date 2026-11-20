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
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idUsuario;

    @NotBlank(message = "Informe seu nome.")
    @Size(max = 40, message = "O nome deve ter no máximo 40 caracteres.")
    @Column(nullable = false, length = 40)
    private String nomeUsuario;

    @NotBlank(message = "Informe o CPF.")
    @Pattern(regexp = "\\d{11}", message = "O CPF deve ter 11 números.")
    @Column(nullable = false, length = 11)
    private String cpfUsuario;

    @NotBlank(message = "Informe o login.")
    @Size(min = 3, max = 30, message = "O login deve ter entre 3 e 30 caracteres.")
    @Column(nullable = false, length = 30)
    private String loginUsuario;

    // Sem tamanho máximo aqui: a senha é gravada criptografada (BCrypt)
    @NotBlank(message = "Informe a senha.")
    @Size(min = 4, message = "A senha deve ter pelo menos 4 caracteres.")
    @Column(nullable = false, length = 150)
    private String senhaUsuario;

    @Column(nullable = false, length = 20)
    private String role = "ROLE_USER";

    // Aceita CPF com ou sem pontuação
    public void setCpfUsuario(String cpfUsuario) {
        this.cpfUsuario = TextoUtil.somenteNumerosOuNulo(cpfUsuario);
    }
}
