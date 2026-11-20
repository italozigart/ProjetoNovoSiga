package com.novosiga.novosiga.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idCurso;

    @NotBlank(message = "Informe o nome do curso.")
    @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres.")
    @Column(nullable = false, length = 50)
    private String nomeCurso;

    @NotBlank(message = "Informe a descrição do curso.")
    @Size(max = 100, message = "A descrição deve ter no máximo 100 caracteres.")
    @Column(nullable = false, length = 100)
    private String descricaoCurso;

    @NotNull(message = "Informe a carga horária.")
    @Positive(message = "A carga horária deve ser maior que zero.")
    @Column(nullable = false)
    private Integer cargaHoraria;
}
