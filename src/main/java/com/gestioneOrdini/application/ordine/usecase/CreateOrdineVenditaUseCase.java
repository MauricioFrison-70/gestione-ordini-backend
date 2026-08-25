package com.gestioneOrdini.application.ordine.usecase;

import com.gestioneOrdini.application.ordine.dto.OrdineVenditaRequest;
import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.repository.AgenteRepository;
import com.gestioneOrdini.domain.ordine.model.OrdineVendita;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateOrdineVenditaUseCase {
    private final OrdineVenditaRepository ordineRepository;
    private final AgenteRepository agenteRepository;

    public CreateOrdineVenditaUseCase(OrdineVenditaRepository ordineRepository,
                                      AgenteRepository agenteRepository) {
        this.ordineRepository = ordineRepository;
        this.agenteRepository = agenteRepository;
    }

    @Transactional
    public OrdineVendita eseguire(OrdineVenditaRequest request) {
        Agente cliente = cercaAgente(request.clienteId());
        Agente venditore = cercaAgente(request.venditoreId());
        Agente trasportatore = cercaAgente(request.trasportatoreId());
        return ordineRepository.save(new OrdineVendita(cliente, venditore, trasportatore));
    }

    private Agente cercaAgente(Long id) {
        return agenteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agente", id));
    }
}
