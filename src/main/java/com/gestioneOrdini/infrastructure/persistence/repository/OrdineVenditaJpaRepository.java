package com.gestioneOrdini.infrastructure.persistence.repository;

import com.gestioneOrdini.infrastructure.persistence.entity.OrdineVenditaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrdineVenditaJpaRepository extends JpaRepository<OrdineVenditaEntity, Long> {
    boolean existsByClienteIdOrVenditoreIdOrTrasportatoreId(
            Long clienteId, Long venditoreId, Long trasportatoreId);

    @Override
    @EntityGraph(attributePaths = {"cliente", "venditore", "trasportatore"})
    Optional<OrdineVenditaEntity> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"cliente", "venditore", "trasportatore"})
    List<OrdineVenditaEntity> findAll();
}
