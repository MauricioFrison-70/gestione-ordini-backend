package com.gestioneOrdini.application.ordine.usecase;

import com.gestioneOrdini.domain.ordine.model.OrdineVendita;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListOrdiniVenditaUseCase {
    private final OrdineVenditaRepository repository;

    public ListOrdiniVenditaUseCase(OrdineVenditaRepository repository) {
        this.repository = repository;
    }

    public List<OrdineVendita> eseguire() { return repository.findAll(); }
}
