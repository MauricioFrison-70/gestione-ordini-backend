package com.gestioneOrdini.application.prodotto.port;

import com.gestioneOrdini.domain.prodotto.event.ScortaRipristinataEvent;
import com.gestioneOrdini.domain.prodotto.event.ScortaSottoMinimoEvent;

/**
 * Porta applicativa per l'invio delle notifiche relative alla scorta.
 */
public interface NotificaScortaService {
    void inviaScortaRipristinata(ScortaRipristinataEvent evento);

    void inviaScortaSottoMinimo(ScortaSottoMinimoEvent evento);
}
