package com.gestioneOrdini.application.prodotto.usecase;

import com.gestioneOrdini.domain.prodotto.repository.ProdottoRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Caso d'uso responsabile dell'eliminazione di un prodotto dal sistema.
 * <p>
 * Garantisce che la rimozione venga eseguita tramite il livello di dominio,
 * mantenendo la separazione tra logica applicativa e persistenza.
 * </p>
 */
@Service
public class DeleteProdottoUseCase {

    private final ProdottoRepository repository;

    public DeleteProdottoUseCase(ProdottoRepository repository) {
        this.repository = repository;
    }

    /**
     * Esegue la cancellazione dell prodotto identificato dal parametro {@code id}.
     *
     * @param id identificatore dell prodotto da eliminare
     * @throws EntityNotFoundException se l'prodotto non esiste
     */
    public void eseguire(Long id) {

        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Prodotto", id);
        }

        repository.deleteById(id);
    }
}