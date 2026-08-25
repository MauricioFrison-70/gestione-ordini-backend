package com.gestioneOrdini.domain.ordine.repository;

import com.gestioneOrdini.domain.ordine.model.OrdineVendita;

import java.util.List;
import java.util.Optional;

public interface OrdineVenditaRepository {
    OrdineVendita save(OrdineVendita ordine);
    Optional<OrdineVendita> findById(Long id);
    List<OrdineVendita> findAll();
    boolean existsByAgenteId(Long agenteId);
    void deleteById(Long id);
}
