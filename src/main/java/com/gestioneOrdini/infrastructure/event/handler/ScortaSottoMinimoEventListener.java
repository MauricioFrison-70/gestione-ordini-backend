package com.gestioneOrdini.infrastructure.event.handler;

import com.gestioneOrdini.application.prodotto.port.NotificaScortaService;
import com.gestioneOrdini.domain.prodotto.event.ScortaSottoMinimoEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ScortaSottoMinimoEventListener {
    private final NotificaScortaService notificaScortaService;

    public ScortaSottoMinimoEventListener(NotificaScortaService notificaScortaService) {
        this.notificaScortaService = notificaScortaService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void gestire(ScortaSottoMinimoEvent evento) {
        notificaScortaService.inviaScortaSottoMinimo(evento);
    }
}
