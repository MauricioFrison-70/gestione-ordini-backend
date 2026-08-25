package com.gestioneOrdini.application.ordine.usecase;

import com.gestioneOrdini.domain.ordine.model.RigaOrdineVendita;
import com.gestioneOrdini.domain.ordine.repository.RigaOrdineVenditaRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetRigaOrdineVenditaUseCase {
    private final RigaOrdineVenditaRepository repository;

    public GetRigaOrdineVenditaUseCase(RigaOrdineVenditaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public RigaOrdineVendita eseguire(Long ordineId, Long rigaId) {
        return repository.findByIdAndOrdineVenditaId(rigaId, ordineId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Riga ordine di vendita", rigaId));
    }
}
