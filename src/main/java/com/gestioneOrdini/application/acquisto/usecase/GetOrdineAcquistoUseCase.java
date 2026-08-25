package com.gestioneOrdini.application.acquisto.usecase;

import com.gestioneOrdini.domain.acquisto.model.OrdineAcquisto;
import com.gestioneOrdini.domain.acquisto.repository.OrdineAcquistoRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GetOrdineAcquistoUseCase {
    private final OrdineAcquistoRepository repository;

    public GetOrdineAcquistoUseCase(OrdineAcquistoRepository repository) {
        this.repository = repository;
    }

    public OrdineAcquisto eseguire(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Ordine di acquisto", id));
    }
}
