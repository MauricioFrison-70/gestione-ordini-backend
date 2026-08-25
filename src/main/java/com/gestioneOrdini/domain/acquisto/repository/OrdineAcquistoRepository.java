package com.gestioneOrdini.domain.acquisto.repository;

import com.gestioneOrdini.domain.acquisto.model.OrdineAcquisto;

import java.util.List;
import java.util.Optional;

public interface OrdineAcquistoRepository {
    OrdineAcquisto save(OrdineAcquisto ordine);
    Optional<OrdineAcquisto> findById(Long id);
    List<OrdineAcquisto> findAll();
    boolean existsByFornitoreId(Long fornitoreId);
    void deleteById(Long id);
}
