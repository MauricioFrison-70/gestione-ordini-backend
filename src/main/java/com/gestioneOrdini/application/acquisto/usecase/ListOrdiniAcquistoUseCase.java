package com.gestioneOrdini.application.acquisto.usecase;

import com.gestioneOrdini.domain.acquisto.model.OrdineAcquisto;
import com.gestioneOrdini.domain.acquisto.repository.OrdineAcquistoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListOrdiniAcquistoUseCase {
    private final OrdineAcquistoRepository repository;

    public ListOrdiniAcquistoUseCase(OrdineAcquistoRepository repository) {
        this.repository = repository;
    }

    public List<OrdineAcquisto> eseguire() {
        return repository.findAll();
    }
}
