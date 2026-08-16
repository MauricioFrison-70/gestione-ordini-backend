package com.gestioneOrdini.infrastructure.event.handler;

import com.gestioneOrdini.domain.prodotto.event.ProdottoCreatoEvent;
import com.gestioneOrdini.domain.prodotto.event.handler.ProdottoCreatoEventHandler;
import org.springframework.stereotype.Service;

@Service
public class ProdottoCreatoEventHandlerImpl implements ProdottoCreatoEventHandler {

    @Override
    public void handle(ProdottoCreatoEvent event) {
        System.out.println("Evento: prodotto creato con ID " + event.getIdProdotto());
        // lógica adicional...
    }
}
