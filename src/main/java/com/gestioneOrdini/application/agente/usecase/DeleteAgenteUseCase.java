package com.gestioneOrdini.application.agente.usecase;

import com.gestioneOrdini.domain.agente.exception.AgenteUtilizzatoException;
import com.gestioneOrdini.domain.agente.repository.AgenteRepository;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Caso d'uso responsabile dell'eliminazione di un agente dal sistema.
 * <p>
 * Garantisce che la rimozione venga eseguita tramite il livello di dominio,
 * mantenendo la separazione tra logica applicativa e persistenza.
 * </p>
 */
@Service
public class DeleteAgenteUseCase {

    private final AgenteRepository repository;
    private final OrdineVenditaRepository ordineVenditaRepository;

    public DeleteAgenteUseCase(AgenteRepository repository,
                               OrdineVenditaRepository ordineVenditaRepository) {
        this.repository = repository;
        this.ordineVenditaRepository = ordineVenditaRepository;
    }

    /**
     * Esegue la cancellazione dell'agente identificato dal parametro {@code id}.
     *
     * @param id identificatore dell'agente da eliminare
     * @throws EntityNotFoundException se l'agente non esiste
     */
    public void eseguire(Long id) {

        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Agente", id);
        }

        if (ordineVenditaRepository.existsByAgenteId(id)) {
            throw new AgenteUtilizzatoException();
        }

        repository.deleteById(id);
    }
}
