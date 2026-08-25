package com.gestioneOrdini.application.ordine.usecase;

import com.gestioneOrdini.domain.ordine.model.RigaOrdineVendita;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import com.gestioneOrdini.domain.ordine.repository.RigaOrdineVenditaRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListRigheOrdineVenditaUseCase {
    private final OrdineVenditaRepository ordineRepository;
    private final RigaOrdineVenditaRepository rigaRepository;

    public ListRigheOrdineVenditaUseCase(
            OrdineVenditaRepository ordineRepository,
            RigaOrdineVenditaRepository rigaRepository) {
        this.ordineRepository = ordineRepository;
        this.rigaRepository = rigaRepository;
    }

    @Transactional(readOnly = true)
    public List<RigaOrdineVendita> eseguire(Long ordineId) {
        if (ordineRepository.findById(ordineId).isEmpty()) {
            throw new EntityNotFoundException("Ordine di vendita", ordineId);
        }
        return rigaRepository.findAllByOrdineVenditaId(ordineId);
    }
}
