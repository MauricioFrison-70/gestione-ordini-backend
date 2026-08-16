package com.gestioneOrdini.application.agente.usecase;

import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.repository.AgenteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Caso d'uso responsabile del recupero dell'elenco completo degli agenti.
 * <p>
 * Mantiene la logica di lettura nel livello applicativo, delegando al dominio
 * l'accesso ai dati e garantendo una chiara separazione delle responsabilità.
 * </p>
 */
@Service
public class ListAgentiUseCase {

    private final AgenteRepository repository;

    public ListAgentiUseCase(AgenteRepository repository) {
        this.repository = repository;
    }

    /**
     * Restituisce l'elenco completo degli agenti presenti nel sistema.
     *
     * @return lista immutabile di modelli di dominio {@link Agente}
     */
    public List<Agente> eseguire() {
        return List.copyOf(repository.findAll());
    }
}
