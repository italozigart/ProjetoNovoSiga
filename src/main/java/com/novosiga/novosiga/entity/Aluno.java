package com.novosiga.novosiga.entity;

import com.novosiga.novosiga.util.TextoUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class Aluno {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idAluno;

    @NotBlank(message = "Informe o nome do aluno.")
    @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres.")
    @Column(nullable = false, length = 50)
    private String nomeAluno;

    @NotBlank(message = "Informe o endereço.")
    @Size(max = 40, message = "O endereço deve ter no máximo 40 caracteres.")
    @Column(nullable = false, length = 40)
    private String enderecoAluno;

    @NotBlank(message = "Informe o bairro.")
    @Size(max = 40, message = "O bairro deve ter no máximo 40 caracteres.")
    @Column(nullable = false, length = 40)
    private String bairroAluno;

    @NotBlank(message = "Informe a cidade.")
    @Size(max = 30, message = "A cidade deve ter no máximo 30 caracteres.")
    @Column(nullable = false, length = 30)
    private String cidadeAluno;

    @NotBlank(message = "Informe o estado.")
    @Pattern(regexp = "[A-Z]{2}", message = "Use a sigla do estado com 2 letras (ex.: SP).")
    @Column(nullable = false, length = 2)
    private String estadoAluno;

    @NotBlank(message = "Informe o CEP.")
    @Pattern(regexp = "\\d{8}", message = "O CEP deve ter 8 números.")
    @Column(nullable = false, length = 8)
    private String cepAluno;

    @NotBlank(message = "Informe o telefone.")
    @Pattern(regexp = "\\d{10,11}", message = "Informe o telefone com DDD (10 ou 11 números).")
    @Column(nullable = false, length = 11)
    private String telefoneAluno;

    @NotBlank(message = "Informe o CPF.")
    @Pattern(regexp = "\\d{11}", message = "O CPF deve ter 11 números.")
    @Column(nullable = false, length = 11)
    private String cpfAluno;

    @Lob //@criando uma string grande
    private byte[] fotoAluno; //byte[] - array que vai guardar os bytes

    @Column(length = 20)
    private String tipoFoto;

    @NotNull(message = "Selecione o curso.")
    @ManyToOne
    @JoinColumn(name = "id_curso_fk")
    private Curso curso;

    // ======================================================
    // SETTERS QUE LIMPAM A MÁSCARA
    // O formulário pode enviar "123.456.789-00"; gravamos "12345678900"
    // ======================================================

    public void setCpfAluno(String cpfAluno) {
        this.cpfAluno = TextoUtil.somenteNumerosOuNulo(cpfAluno);
    }

    public void setTelefoneAluno(String telefoneAluno) {
        this.telefoneAluno = TextoUtil.somenteNumerosOuNulo(telefoneAluno);
    }

    public void setCepAluno(String cepAluno) {
        this.cepAluno = TextoUtil.somenteNumerosOuNulo(cepAluno);
    }

    public void setEstadoAluno(String estadoAluno) {
        this.estadoAluno = TextoUtil.maiusculo(estadoAluno);
    }

    // ======================================================
    // MÉTODOS USADOS NAS TELAS
    // ======================================================

    public String getCpfFormatado() {
        return TextoUtil.formatarCpf(cpfAluno);
    }

    public String getTelefoneFormatado() {
        return TextoUtil.formatarTelefone(telefoneAluno);
    }

    public String getCepFormatado() {
        return TextoUtil.formatarCep(cepAluno);
    }

    public boolean isTemFoto() {
        return fotoAluno != null && fotoAluno.length > 0;
    }
}
