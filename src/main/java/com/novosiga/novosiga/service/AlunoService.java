package com.novosiga.novosiga.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.novosiga.novosiga.busca.Busca;
import com.novosiga.novosiga.entity.Aluno;
import com.novosiga.novosiga.entity.Curso;
import com.novosiga.novosiga.exception.NegocioException;
import com.novosiga.novosiga.repository.AlunoRepository;
import com.novosiga.novosiga.repository.PedidoRepository;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

@Service
public class AlunoService {

    // injeção de dependência do repositório de alunos
    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    // método para salvar um aluno
    public Aluno save(Aluno aluno) {
        return alunoRepository.save(aluno);
    }

    // método para listar todos os alunos (ordem alfabética)
    public List<Aluno> findAll() {
        return alunoRepository.findAll(Sort.by("nomeAluno"));
    }

    // método para buscar aluno pelo ID
    public Aluno findById(Integer id) {
        return alunoRepository.findById(id).orElse(null);
    }

    public long contar() {
        return alunoRepository.count();
    }

    // Busca paginada por nome, endereço, cidade, curso, CPF ou telefone
    public Page<Aluno> buscar(String termo, int pagina, int tamanho) {
        Specification<Aluno> spec = Busca.porTermo(termo,
                root -> {
                    Join<Aluno, Curso> curso = root.join("curso", JoinType.LEFT);
                    return List.of(
                            root.<String>get("nomeAluno"),
                            root.<String>get("enderecoAluno"),
                            root.<String>get("bairroAluno"),
                            root.<String>get("cidadeAluno"),
                            curso.<String>get("nomeCurso"));
                },
                root -> List.of(root.<String>get("cpfAluno"), root.<String>get("telefoneAluno")));

        return alunoRepository.findAll(spec, PageRequest.of(Math.max(pagina, 0), tamanho, Sort.by("nomeAluno")));
    }

    // método para excluir um aluno pelo ID (bloqueia se ele tiver pedidos)
    public void excluir(Integer id) {
        Aluno aluno = findById(id);
        if (aluno == null) {
            throw new NegocioException("Aluno não encontrado.");
        }
        long pedidos = pedidoRepository.countByAluno(aluno);
        if (pedidos > 0) {
            throw new NegocioException("Não é possível excluir \"" + aluno.getNomeAluno()
                    + "\": ele possui " + pedidos + " pedido(s) registrado(s).");
        }
        alunoRepository.delete(aluno);
    }
}
