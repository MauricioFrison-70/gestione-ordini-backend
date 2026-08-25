package com.gestioneOrdini.application.ordine.usecase;

import com.gestioneOrdini.domain.ordine.model.OrdineVendita;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GetOrdineVenditaUseCase {
    private final OrdineVenditaRepository repository;

    public GetOrdineVenditaUseCase(OrdineVenditaRepository repository) {
        this.repository = repository;
    }

    public OrdineVendita eseguire(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ordine di vendita", id));
    }
}
