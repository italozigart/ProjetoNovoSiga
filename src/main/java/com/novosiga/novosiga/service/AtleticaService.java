package com.novosiga.novosiga.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.novosiga.novosiga.busca.Busca;
import com.novosiga.novosiga.entity.Atletica;
import com.novosiga.novosiga.entity.Curso;
import com.novosiga.novosiga.exception.NegocioException;
import com.novosiga.novosiga.repository.AtleticaRepository;
import com.novosiga.novosiga.repository.ProdutoRepository;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

@Service
public class AtleticaService {

    @Autowired
    private AtleticaRepository atleticaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    public Atletica save(Atletica atletica) {
        return atleticaRepository.save(atletica);
    }

    // Lista em ordem alfabética (selects e filtros da loja)
    public List<Atletica> findAll() {
        return atleticaRepository.findAll(Sort.by("nomeAtletica"));
    }

    public Atletica findById(Integer id) {
        return atleticaRepository.findById(id).orElse(null);
    }

    public long contar() {
        return atleticaRepository.count();
    }

    // Busca paginada por nome, descrição, Instagram ou curso
    public Page<Atletica> buscar(String termo, int pagina, int tamanho) {
        Specification<Atletica> spec = Busca.porTermo(termo,
                root -> {
                    Join<Atletica, Curso> curso = root.join("curso", JoinType.LEFT);
                    return List.of(
                            root.<String>get("nomeAtletica"),
                            root.<String>get("descricaoAtletica"),
                            root.<String>get("instagramAtletica"),
                            curso.<String>get("nomeCurso"));
                },
                null);

        return atleticaRepository.findAll(spec,
                PageRequest.of(Math.max(pagina, 0), tamanho, Sort.by("nomeAtletica")));
    }

    // Exclui a atlética somente se ela não tiver produtos cadastrados
    public void excluir(Integer id) {
        Atletica atletica = findById(id);
        if (atletica == null) {
            throw new NegocioException("Atlética não encontrada.");
        }
        long produtos = produtoRepository.countByAtletica(atletica);
        if (produtos > 0) {
            throw new NegocioException("Não é possível excluir \"" + atletica.getNomeAtletica()
                    + "\": ela possui " + produtos + " produto(s) cadastrado(s).");
        }
        atleticaRepository.delete(atletica);
    }
}
