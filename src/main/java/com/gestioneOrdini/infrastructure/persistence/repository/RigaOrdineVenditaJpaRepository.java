package com.gestioneOrdini.infrastructure.persistence.repository;

import com.gestioneOrdini.infrastructure.persistence.entity.RigaOrdineVenditaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RigaOrdineVenditaJpaRepository
        extends JpaRepository<RigaOrdineVenditaEntity, Long> {

    @EntityGraph(attributePaths = "prodotto")
    Optional<RigaOrdineVenditaEntity> findByIdAndOrdineVenditaId(
            Long id, Long ordineVenditaId);

    @EntityGraph(attributePaths = "prodotto")
    List<RigaOrdineVenditaEntity> findAllByOrdineVenditaIdOrderById(
            Long ordineVenditaId);

    boolean existsByOrdineVenditaIdAndProdottoId(
            Long ordineVenditaId, Long prodottoId);

    boolean existsByOrdineVenditaIdAndProdottoIdAndIdNot(
            Long ordineVenditaId, Long prodottoId, Long id);

    void deleteByIdAndOrdineVenditaId(Long id, Long ordineVenditaId);
}
