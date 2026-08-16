package com.gestioneOrdini.domain.prodotto.event.handler;

import com.gestioneOrdini.domain.prodotto.event.ProdottoCreatoEvent;

public interface ProdottoCreatoEventHandler {
    void handle(ProdottoCreatoEvent event);
}
