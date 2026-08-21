package com.gestioneOrdini.application.ordine.usecase;

import com.gestioneOrdini.application.ordine.dto.OrdineVenditaRequest;
import com.gestioneOrdini.domain.agente.repository.AgenteRepository;
import com.gestioneOrdini.domain.ordine.model.OrdineVendita;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateOrdineVenditaUseCase {
    private final OrdineVenditaRepository ordineRepository;
    private final AgenteRepository agenteRepository;

    public UpdateOrdineVenditaUseCase(OrdineVenditaRepository ordineRepository,
                                      AgenteRepository agenteRepository) {
        this.ordineRepository = ordineRepository;
        this.agenteRepository = agenteRepository;
    }

    @Transactional
    public OrdineVendita eseguire(Long id, OrdineVenditaRequest request) {
        OrdineVendita ordine = ordineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ordine di vendita", id));
        ordine.setCliente(cercaAgente(request.clienteId()));
        ordine.setVenditore(cercaAgente(request.venditoreId()));
        ordine.setTrasportatore(cercaAgente(request.trasportatoreId()));
        ordine.setDataRilascio(request.dataRilascio());
        return ordineRepository.save(ordine);
    }

    private com.gestioneOrdini.domain.agente.model.Agente cercaAgente(Long id) {
        return agenteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agente", id));
    }
}
