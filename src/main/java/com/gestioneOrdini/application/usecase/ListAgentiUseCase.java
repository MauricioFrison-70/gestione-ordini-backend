package com.gestioneOrdini.application.usecase;

import com.gestioneOrdini.domain.repository.AgenteRepository;
import com.gestioneOrdini.infrastructure.persistence.entity.AgenteEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Caso d'uso dedicato all'elenco completo degli agenti presenti nel sistema.
 * <p>
 * Questa classe centralizza la logica di lettura massiva, delegando al
 * {@link AgenteRepository} il recupero dei dati e mantenendo un flusso
 * coerente con i principi dell'architettura applicativa.
 * </p>
 *
 * <p><strong>Responsabilità:</strong></p>
 * <ul>
 *     <li>Richiamare il repository per ottenere tutti gli agenti registrati.</li>
 *     <li>Restituire una lista immutabile o non modificabile dal chiamante (a seconda dell'implementazione).</li>
 * </ul>
 *
 * <p>
 * Questo caso d'uso facilita la separazione tra logica di business e
 * persistenza, migliorando la manutenibilità e la testabilità del sistema.
 * </p>
 */

@Service
public class ListAgentiUseCase {

    private final AgenteRepository repository;

    public ListAgentiUseCase(AgenteRepository repository) {
        this.repository = repository;
    }

    public List<AgenteEntity> executar() {
        return repository.findAll();
    }
}

