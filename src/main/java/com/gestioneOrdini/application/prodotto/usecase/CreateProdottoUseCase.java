package com.gestioneOrdini.application.prodotto.usecase;

import com.gestioneOrdini.domain.prodotto.event.ProdottoCreatoEvent;
import com.gestioneOrdini.domain.prodotto.event.handler.ProdottoCreatoEventHandler;
import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import com.gestioneOrdini.domain.prodotto.repository.ProdottoRepository;
import org.springframework.stereotype.Service;

/**
 * Caso d'uso responsabile della creazione di un nuovo {@link Prodotto}
 * all'interno del sistema.
 *
 * <p>Incapsula la logica applicativa necessaria per registrare un nuovo
 * prodotto, delegando la persistenza al {@link ProdottoRepository}. Dopo la
 * creazione viene generato un evento di dominio {@link ProdottoCreatoEvent}
 * per notificare altri componenti interessati.</p>
 *
 * @author Mauricio
 * @version 2.0
 */
@Service
public class CreateProdottoUseCase {

    private final ProdottoRepository repository;
    private final ProdottoCreatoEventHandler eventHandler;

    public CreateProdottoUseCase(ProdottoRepository repository,
                                 ProdottoCreatoEventHandler eventHandler) {
        this.repository = repository;
        this.eventHandler = eventHandler;
    }

    /**
     * Esegue la creazione di un nuovo prodotto e genera un evento di dominio.
     *
     * @param prodotto modello di dominio contenente i dati dell prodotto
     * @return l'entità persistita
     */
    public Prodotto eseguire(Prodotto prodotto) {

        // Persistência delegada ao repositório do domínio
        Prodotto salvato = repository.save(prodotto);

        // Evento de domínio
        var event = new ProdottoCreatoEvent(
                salvato.getId(),
                salvato.getCodice(),
                salvato.getDescrizione(),
                salvato.getValoreAcquisto(),
                salvato.getValoreVendita(),
                salvato.getQuantita(),
                salvato.getScortaMinima()
        );

        eventHandler.handle(event);

        return salvato;
    }
}
