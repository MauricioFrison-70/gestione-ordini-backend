package com.gestioneOrdini.application.prodotto.usecase;

import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import com.gestioneOrdini.domain.prodotto.repository.ProdottoRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Caso d'uso responsabile del recupero di un prodotto tramite il suo identificatore.
 * <p>
 * Mantiene la logica di lettura nel livello applicativo, delegando al dominio
 * l'accesso ai dati e garantendo una chiara separazione delle responsabilità.
 * </p>
 */
@Service
public class GetProdottoUseCase {

    private final ProdottoRepository repository;

    public GetProdottoUseCase(ProdottoRepository repository) {
        this.repository = repository;
    }

    /**
     * Recupera l'prodotto corrispondente all'identificatore fornito.
     *
     * @param id identificatore dell prodotto
     * @return modello di dominio dell prodotto
     * @throws EntityNotFoundException se il prodotto non esiste
     */
    public Prodotto eseguire(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Prodotto", id));
    }
}
