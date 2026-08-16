package com.gestioneOrdini.application.agente.usecase;

import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.repository.AgenteRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Caso d'uso responsabile del recupero di un agente tramite il suo identificatore.
 * <p>
 * Mantiene la logica di lettura nel livello applicativo, delegando al dominio
 * l'accesso ai dati e garantendo una chiara separazione delle responsabilità.
 * </p>
 */
@Service
public class GetAgenteUseCase {

    private final AgenteRepository repository;

    public GetAgenteUseCase(AgenteRepository repository) {
        this.repository = repository;
    }

    /**
     * Recupera l'agente corrispondente all'identificatore fornito.
     *
     * @param id identificatore dell'agente
     * @return modello di dominio dell'agente
     * @throws EntityNotFoundException se l'agente non esiste
     */
    public Agente eseguire(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agente", id));
    }

}
