package com.gestioneOrdini.application.prodotto.usecase;

import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import com.gestioneOrdini.domain.prodotto.repository.ProdottoRepository;
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
public class ListProdottiUseCase {

    private final ProdottoRepository repository;

    public ListProdottiUseCase(ProdottoRepository repository) {
        this.repository = repository;
    }

    /**
     * Restituisce l'elenco completo degli agenti presenti nel sistema.
     *
     * @return lista immutabile di modelli di dominio {@link Prodotto}
     */
    public List<Prodotto> eseguire() {
        return List.copyOf(repository.findAll());
    }
}
