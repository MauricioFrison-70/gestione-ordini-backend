package com.gestioneOrdini.application.acquisto.usecase;

import com.gestioneOrdini.domain.acquisto.model.RigaOrdineAcquisto;
import com.gestioneOrdini.domain.acquisto.repository.OrdineAcquistoRepository;
import com.gestioneOrdini.domain.acquisto.repository.RigaOrdineAcquistoRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListRigheOrdineAcquistoUseCase {
    private final OrdineAcquistoRepository ordineRepository;
    private final RigaOrdineAcquistoRepository rigaRepository;

    public ListRigheOrdineAcquistoUseCase(
            OrdineAcquistoRepository ordineRepository,
            RigaOrdineAcquistoRepository rigaRepository) {
        this.ordineRepository = ordineRepository;
        this.rigaRepository = rigaRepository;
    }

    public List<RigaOrdineAcquisto> eseguire(Long ordineId) {
        if (ordineRepository.findById(ordineId).isEmpty()) {
            throw new EntityNotFoundException("Ordine di acquisto", ordineId);
        }
        return rigaRepository.findAllByOrdineAcquistoId(ordineId);
    }
}
