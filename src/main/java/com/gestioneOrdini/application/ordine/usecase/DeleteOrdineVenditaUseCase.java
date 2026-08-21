package com.gestioneOrdini.application.ordine.usecase;

import com.gestioneOrdini.domain.ordine.exception.OrdineVenditaRilasciatoException;
import com.gestioneOrdini.domain.ordine.model.OrdineVendita;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteOrdineVenditaUseCase {
    private final OrdineVenditaRepository repository;

    public DeleteOrdineVenditaUseCase(OrdineVenditaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void eseguire(Long id) {
        OrdineVendita ordine = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ordine di vendita", id));
        if (ordine.getDataRilascio() != null) {
            throw new OrdineVenditaRilasciatoException(ordine.getNumeroOrdine());
        }
        repository.deleteById(id);
    }
}
