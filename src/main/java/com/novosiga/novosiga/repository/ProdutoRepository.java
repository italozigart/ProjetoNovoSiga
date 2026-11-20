package com.novosiga.novosiga.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.novosiga.novosiga.entity.Atletica;
import com.novosiga.novosiga.entity.Produto;

import jakarta.persistence.LockModeType;

public interface ProdutoRepository extends JpaRepository<Produto, Integer>, JpaSpecificationExecutor<Produto> {

    // Categorias já usadas nos produtos (filtro da loja)
    @Query("select distinct p.categoriaProduto from Produto p "
            + "where p.categoriaProduto is not null order by p.categoriaProduto")
    List<String> listarCategorias();

    // Busca o produto travando a linha no banco (SELECT ... FOR UPDATE).
    // Usado na finalização da compra: se duas pessoas comprarem o último item
    // ao mesmo tempo, a segunda espera a primeira terminar e vê o estoque atualizado.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Produto p where p.idProduto = :id")
    Optional<Produto> buscarParaAtualizarEstoque(@Param("id") Integer id);

    // Outros produtos da mesma atlética (página de detalhes)
    @Query("select p from Produto p where p.atletica = :atletica and p.idProduto <> :idProduto "
            + "order by p.idProduto desc")
    List<Produto> buscarRelacionados(@Param("atletica") Atletica atletica,
            @Param("idProduto") Integer idProduto, Pageable pageable);

    // Produtos mais recentes com estoque (destaques da tela inicial)
    @Query("select p from Produto p where p.estoqueProduto > 0 order by p.idProduto desc")
    List<Produto> buscarDestaques(Pageable pageable);

    long countByAtletica(Atletica atletica);
}
