package com.gestioneOrdini.application.acquisto.usecase;

import com.gestioneOrdini.application.acquisto.dto.OrdineAcquistoRequest;
import com.gestioneOrdini.domain.acquisto.model.OrdineAcquisto;
import com.gestioneOrdini.domain.acquisto.repository.OrdineAcquistoRepository;
import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.repository.AgenteRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateOrdineAcquistoUseCase {
    private final OrdineAcquistoRepository ordineRepository;
    private final AgenteRepository agenteRepository;

    public CreateOrdineAcquistoUseCase(
            OrdineAcquistoRepository ordineRepository,
            AgenteRepository agenteRepository) {
        this.ordineRepository = ordineRepository;
        this.agenteRepository = agenteRepository;
    }

    @Transactional
    public OrdineAcquisto eseguire(OrdineAcquistoRequest request) {
        Agente fornitore = agenteRepository.findById(request.fornitoreId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Fornitore", request.fornitoreId()));
        if (Boolean.TRUE.equals(fornitore.getArchiviato())) {
            throw new IllegalArgumentException(
                    "Un fornitore archiviato non può essere utilizzato nell'ordine di acquisto");
        }
        return ordineRepository.save(new OrdineAcquisto(fornitore));
    }
}
