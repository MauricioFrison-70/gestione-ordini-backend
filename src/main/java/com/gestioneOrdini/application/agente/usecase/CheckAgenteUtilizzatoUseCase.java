package com.gestioneOrdini.application.agente.usecase;

import com.gestioneOrdini.domain.agente.repository.AgenteRepository;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CheckAgenteUtilizzatoUseCase {
    private final AgenteRepository agenteRepository;
    private final OrdineVenditaRepository ordineVenditaRepository;

    public CheckAgenteUtilizzatoUseCase(AgenteRepository agenteRepository,
                                        OrdineVenditaRepository ordineVenditaRepository) {
        this.agenteRepository = agenteRepository;
        this.ordineVenditaRepository = ordineVenditaRepository;
    }

    public boolean eseguire(Long id) {
        if (!agenteRepository.existsById(id)) {
            throw new EntityNotFoundException("Agente", id);
        }
        return ordineVenditaRepository.existsByAgenteId(id);
    }
}
