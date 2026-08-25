package com.gestioneOrdini.application.ordine.usecase;

import com.gestioneOrdini.domain.ordine.model.OrdineVendita;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import com.gestioneOrdini.domain.ordine.repository.RigaOrdineVenditaRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteRigaOrdineVenditaUseCase {
    private final OrdineVenditaRepository ordineRepository;
    private final RigaOrdineVenditaRepository rigaRepository;

    public DeleteRigaOrdineVenditaUseCase(
            OrdineVenditaRepository ordineRepository,
            RigaOrdineVenditaRepository rigaRepository) {
        this.ordineRepository = ordineRepository;
        this.rigaRepository = rigaRepository;
    }

    @Transactional
    public void eseguire(Long ordineId, Long rigaId) {
        OrdineVendita ordine = ordineRepository.findById(ordineId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Ordine di vendita", ordineId));
        ordine.verificaModificabile();
        if (rigaRepository.findByIdAndOrdineVenditaId(rigaId, ordineId).isEmpty()) {
            throw new EntityNotFoundException("Riga ordine di vendita", rigaId);
        }
        rigaRepository.deleteByIdAndOrdineVenditaId(rigaId, ordineId);
    }
}
