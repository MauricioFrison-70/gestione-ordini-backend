package com.gestioneOrdini.infrastructure.persistence.repository;

import com.gestioneOrdini.infrastructure.persistence.entity.RigaOrdineAcquistoEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RigaOrdineAcquistoJpaRepository
        extends JpaRepository<RigaOrdineAcquistoEntity, Long> {

    @EntityGraph(attributePaths = {"ordineAcquisto", "prodotto"})
    Optional<RigaOrdineAcquistoEntity> findByIdAndOrdineAcquistoId(
            Long id, Long ordineAcquistoId);

    @EntityGraph(attributePaths = {"ordineAcquisto", "prodotto"})
    List<RigaOrdineAcquistoEntity> findAllByOrdineAcquistoIdOrderById(
            Long ordineAcquistoId);

    boolean existsByOrdineAcquistoIdAndProdottoId(
            Long ordineAcquistoId, Long prodottoId);

    boolean existsByOrdineAcquistoIdAndProdottoIdAndIdNot(
            Long ordineAcquistoId, Long prodottoId, Long id);

    void deleteByIdAndOrdineAcquistoId(Long id, Long ordineAcquistoId);
}
