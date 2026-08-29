package com.gestioneOrdini.domain.reporting.repository;

import com.gestioneOrdini.domain.reporting.model.Rapporto;

import java.util.List;
import java.util.Optional;

public interface RapportoRepository {
    List<Rapporto> findAllAttivi();
    Optional<Rapporto> findById(Long id);
    Optional<Rapporto> findByCodice(String codice);
}
