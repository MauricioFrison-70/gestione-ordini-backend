package com.gestioneOrdini.infrastructure.persistence.repository;

import com.gestioneOrdini.infrastructure.persistence.entity.OrdineAcquistoEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrdineAcquistoJpaRepository
        extends JpaRepository<OrdineAcquistoEntity, Long> {

    boolean existsByFornitoreId(Long fornitoreId);

    @Override
    @EntityGraph(attributePaths = "fornitore")
    Optional<OrdineAcquistoEntity> findById(Long id);

    @Override
    @EntityGraph(attributePaths = "fornitore")
    List<OrdineAcquistoEntity> findAll();
}
