package com.gestioneOrdini.application.reporting.usecase;

import com.gestioneOrdini.domain.reporting.model.Rapporto;
import com.gestioneOrdini.domain.reporting.repository.RapportoRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GetRapportoUseCase {

    private final RapportoRepository repository;

    public GetRapportoUseCase(RapportoRepository repository) {
        this.repository = repository;
    }

    public Rapporto eseguire(Long id) {
        Rapporto rapporto = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rapporto", id));
        if (!rapporto.isAttivo()) {
            throw new EntityNotFoundException("Rapporto", id);
        }
        return rapporto;
    }
}
