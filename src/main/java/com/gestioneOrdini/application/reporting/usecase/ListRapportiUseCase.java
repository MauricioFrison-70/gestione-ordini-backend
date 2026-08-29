package com.gestioneOrdini.application.reporting.usecase;

import com.gestioneOrdini.domain.reporting.model.Rapporto;
import com.gestioneOrdini.domain.reporting.repository.RapportoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListRapportiUseCase {

    private final RapportoRepository repository;

    public ListRapportiUseCase(RapportoRepository repository) {
        this.repository = repository;
    }

    public List<Rapporto> eseguire() {
        return List.copyOf(repository.findAllAttivi());
    }
}
