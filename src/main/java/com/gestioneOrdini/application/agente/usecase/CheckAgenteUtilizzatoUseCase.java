package com.gestioneOrdini.application.agente.usecase;

import com.gestioneOrdini.domain.agente.repository.AgenteRepository;
import com.gestioneOrdini.domain.acquisto.repository.OrdineAcquistoRepository;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CheckAgenteUtilizzatoUseCase {
    private final AgenteRepository agenteRepository;
    private final OrdineVenditaRepository ordineVenditaRepository;
    private final OrdineAcquistoRepository ordineAcquistoRepository;

    public CheckAgenteUtilizzatoUseCase(AgenteRepository agenteRepository,
                                        OrdineVenditaRepository ordineVenditaRepository,
                                        OrdineAcquistoRepository ordineAcquistoRepository) {
        this.agenteRepository = agenteRepository;
        this.ordineVenditaRepository = ordineVenditaRepository;
        this.ordineAcquistoRepository = ordineAcquistoRepository;
    }

    public boolean eseguire(Long id) {
        if (!agenteRepository.existsById(id)) {
            throw new EntityNotFoundException("Agente", id);
        }
        return ordineVenditaRepository.existsByAgenteId(id)
                || ordineAcquistoRepository.existsByFornitoreId(id);
    }
}
