package com.gestioneOrdini.domain.ordine.repository;

import com.gestioneOrdini.domain.ordine.model.RigaOrdineVendita;

import java.util.List;
import java.util.Optional;

public interface RigaOrdineVenditaRepository {
    RigaOrdineVendita save(RigaOrdineVendita riga);
    Optional<RigaOrdineVendita> findByIdAndOrdineVenditaId(Long id, Long ordineVenditaId);
    List<RigaOrdineVendita> findAllByOrdineVenditaId(Long ordineVenditaId);
    boolean existsByOrdineVenditaIdAndProdottoId(Long ordineVenditaId, Long prodottoId);
    boolean existsByOrdineVenditaIdAndProdottoIdAndIdNot(
            Long ordineVenditaId, Long prodottoId, Long idEscluso);
    void deleteByIdAndOrdineVenditaId(Long id, Long ordineVenditaId);
}
