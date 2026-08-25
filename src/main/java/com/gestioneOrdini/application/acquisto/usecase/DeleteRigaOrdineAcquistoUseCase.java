package com.gestioneOrdini.application.acquisto.usecase;

import com.gestioneOrdini.domain.acquisto.model.OrdineAcquisto;
import com.gestioneOrdini.domain.acquisto.repository.OrdineAcquistoRepository;
import com.gestioneOrdini.domain.acquisto.repository.RigaOrdineAcquistoRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteRigaOrdineAcquistoUseCase {
    private final OrdineAcquistoRepository ordineRepository;
    private final RigaOrdineAcquistoRepository rigaRepository;

    public DeleteRigaOrdineAcquistoUseCase(
            OrdineAcquistoRepository ordineRepository,
            RigaOrdineAcquistoRepository rigaRepository) {
        this.ordineRepository = ordineRepository;
        this.rigaRepository = rigaRepository;
    }

    @Transactional
    public void eseguire(Long ordineId, Long rigaId) {
        OrdineAcquisto ordine = ordineRepository.findById(ordineId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Ordine di acquisto", ordineId));
        ordine.verificaModificabile();
        if (rigaRepository.findByIdAndOrdineAcquistoId(rigaId, ordineId).isEmpty()) {
            throw new EntityNotFoundException("Riga ordine di acquisto", rigaId);
        }
        rigaRepository.deleteByIdAndOrdineAcquistoId(rigaId, ordineId);
    }
}
