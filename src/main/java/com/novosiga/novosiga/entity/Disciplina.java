package com.novosiga.novosiga.entity;

import com.novosiga.novosiga.util.TextoUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class Disciplina {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idDisciplina;

    @NotBlank(message = "Informe o nome da disciplina.")
    @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres.")
    @Column(nullable = false, length = 50)
    private String nomeDisciplina;

    @NotBlank(message = "Informe a sigla.")
    @Size(max = 10, message = "A sigla deve ter no máximo 10 caracteres.")
    @Column(nullable = false, length = 10)
    private String siglaDisciplina;

    @NotNull(message = "Informe a carga horária.")
    @Positive(message = "A carga horária deve ser maior que zero.")
    @Column(nullable = false)
    private Integer cargaHorariaDisciplina;


    // Relacionamento com Curso
    @NotNull(message = "Selecione o curso.")
    @ManyToOne
    @JoinColumn(name = "id_curso_fk", nullable = false)
    private Curso curso;


    // Relacionamento com Professor
    @NotNull(message = "Selecione o professor.")
    @ManyToOne
    @JoinColumn(name = "id_professor_fk", nullable = false)
    private Professor professor;

    // A sigla é sempre gravada em maiúsculas (ex.: "bd" -> "BD")
    public void setSiglaDisciplina(String siglaDisciplina) {
        this.siglaDisciplina = TextoUtil.maiusculo(siglaDisciplina);
    }
}
