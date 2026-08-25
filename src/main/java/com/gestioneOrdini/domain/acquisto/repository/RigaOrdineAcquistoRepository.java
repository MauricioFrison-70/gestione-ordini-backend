package com.gestioneOrdini.domain.acquisto.repository;

import com.gestioneOrdini.domain.acquisto.model.RigaOrdineAcquisto;

import java.util.List;
import java.util.Optional;

public interface RigaOrdineAcquistoRepository {
    RigaOrdineAcquisto save(RigaOrdineAcquisto riga);
    Optional<RigaOrdineAcquisto> findByIdAndOrdineAcquistoId(Long id, Long ordineId);
    List<RigaOrdineAcquisto> findAllByOrdineAcquistoId(Long ordineId);
    boolean existsByOrdineAcquistoIdAndProdottoId(Long ordineId, Long prodottoId);
    boolean existsByOrdineAcquistoIdAndProdottoIdAndIdNot(
            Long ordineId, Long prodottoId, Long idEscluso);
    void deleteByIdAndOrdineAcquistoId(Long id, Long ordineId);
}
