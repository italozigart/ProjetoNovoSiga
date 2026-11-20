package com.novosiga.novosiga.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.novosiga.novosiga.busca.Busca;
import com.novosiga.novosiga.entity.Professor;
import com.novosiga.novosiga.exception.NegocioException;
import com.novosiga.novosiga.repository.DisciplinaRepository;
import com.novosiga.novosiga.repository.ProfessorRepository;

@Service
public class ProfessorService {

    // injeção de dependência do repositório de professores
    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    // método para salvar um professor
    public Professor save(Professor professor) {
        return professorRepository.save(professor);
    }

    // método para listar todos os professores (ordem alfabética)
    public List<Professor> findAll() {
        return professorRepository.findAll(Sort.by("nomeProfessor"));
    }

    // método para buscar professor pelo ID
    public Professor findById(Integer id) {
        return professorRepository.findById(id).orElse(null);
    }

    public long contar() {
        return professorRepository.count();
    }

    // Busca paginada por nome, graduação, RG, endereço, CPF ou telefone
    public Page<Professor> buscar(String termo, int pagina, int tamanho) {
        Specification<Professor> spec = Busca.porTermo(termo,
                root -> List.of(
                        root.<String>get("nomeProfessor"),
                        root.<String>get("graduacaoProfessor"),
                        root.<String>get("rgProfessor"),
                        root.<String>get("enderecoProfessor")),
                root -> List.of(root.<String>get("cpfProfessor"), root.<String>get("telefoneProfessor")));

        return professorRepository.findAll(spec,
                PageRequest.of(Math.max(pagina, 0), tamanho, Sort.by("nomeProfessor")));
    }

    // método para excluir um professor (bloqueia se ele lecionar alguma disciplina)
    public void excluir(Integer id) {
        Professor professor = findById(id);
        if (professor == null) {
            throw new NegocioException("Professor não encontrado.");
        }
        long disciplinas = disciplinaRepository.countByProfessor(professor);
        if (disciplinas > 0) {
            throw new NegocioException("Não é possível excluir \"" + professor.getNomeProfessor()
                    + "\": ele leciona " + disciplinas + " disciplina(s). Troque o professor dessas disciplinas antes.");
        }
        professorRepository.delete(professor);
    }
}
