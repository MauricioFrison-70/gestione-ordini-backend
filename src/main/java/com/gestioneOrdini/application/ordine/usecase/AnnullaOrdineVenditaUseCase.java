package com.gestioneOrdini.application.ordine.usecase;

import com.gestioneOrdini.domain.ordine.model.OrdineVendita;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class AnnullaOrdineVenditaUseCase {
    private final OrdineVenditaRepository repository;

    public AnnullaOrdineVenditaUseCase(OrdineVenditaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public OrdineVendita eseguire(Long id) {
        OrdineVendita ordine = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ordine di vendita", id));
        ordine.annulla(LocalDate.now());
        return repository.save(ordine);
    }
}
