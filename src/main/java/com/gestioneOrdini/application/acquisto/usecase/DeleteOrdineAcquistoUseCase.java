package com.gestioneOrdini.application.acquisto.usecase;

import com.gestioneOrdini.domain.acquisto.model.OrdineAcquisto;
import com.gestioneOrdini.domain.acquisto.repository.OrdineAcquistoRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteOrdineAcquistoUseCase {
    private final OrdineAcquistoRepository repository;

    public DeleteOrdineAcquistoUseCase(OrdineAcquistoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void eseguire(Long id) {
        OrdineAcquisto ordine = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Ordine di acquisto", id));
        ordine.verificaModificabile();
        repository.deleteById(id);
    }
}
