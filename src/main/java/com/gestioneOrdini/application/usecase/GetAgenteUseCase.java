package com.gestioneOrdini.application.usecase;

import com.gestioneOrdini.domain.model.Agente;
import com.gestioneOrdini.domain.repository.AgenteRepository;
import org.springframework.stereotype.Service;

/**
 * Caso d'uso dedicato al recupero di un agente tramite il suo identificatore.
 * <p>
 * Questa classe centralizza la logica di lettura, delegando al
 * {@link AgenteRepository} l'accesso ai dati e garantendo un flusso
 * coerente all'interno dell'architettura applicativa.
 * </p>
 *
 * <p><strong>Responsabilità:</strong></p>
 * <ul>
 *     <li>Ricevere l'ID dell'agente richiesto.</li>
 *     <li>Interrogare il repository per ottenere l'entità corrispondente.</li>
 *     <li>Generare un'eccezione significativa se l'agente non esiste.</li>
 * </ul>
 *
 * <p>
 * L'obiettivo è mantenere separata la logica di business dalla logica
 * di persistenza, facilitando test, manutenzione e riutilizzo.
 * </p>
 */

@Service
public class GetAgenteUseCase {

    private final AgenteRepository repository;

    public GetAgenteUseCase(AgenteRepository repository) {
        this.repository = repository;
    }

    public Agente executar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Agente non trovato"));
    }

}
