package com.gestioneOrdini.application.usecase;

import com.gestioneOrdini.domain.repository.AgenteRepository;
import org.springframework.stereotype.Service;

/**
 * Caso d'uso responsabile dell'eliminazione di un agente dal sistema.
 * <p>
 * Questa classe incapsula la logica di rimozione, garantendo che
 * l'operazione venga eseguita tramite il repository appropriato
 * all'interno del livello di dominio.
 * </p>
 *
 * <p><strong>Responsabilità:</strong></p>
 * <ul>
 *     <li>Ricevere l'identificatore dell'agente da eliminare.</li>
 *     <li>Delegare al {@link AgenteRepository} l'operazione di cancellazione.</li>
 * </ul>
 *
 * <p>
 * Fa parte dell'architettura applicativa e contribuisce a mantenere
 * la separazione delle responsabilità, facilitando inoltre i test unitari.
 * </p>
 */

@Service
public class DeleteAgenteUseCase {

    private final AgenteRepository repository;

    public DeleteAgenteUseCase(AgenteRepository repository) {
        this.repository = repository;
    }

    public void executar(Long id) {
        repository.deleteById(id);
    }
}

