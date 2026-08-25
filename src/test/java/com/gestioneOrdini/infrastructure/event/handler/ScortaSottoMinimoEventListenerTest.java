package com.gestioneOrdini.infrastructure.event.handler;

import com.gestioneOrdini.application.prodotto.port.NotificaScortaService;
import com.gestioneOrdini.domain.prodotto.event.ScortaSottoMinimoEvent;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ScortaSottoMinimoEventListenerTest {

    @Test
    void dovrebbeDelegareLaNotificaAlServizioConfigurato() {
        NotificaScortaService service = mock(NotificaScortaService.class);
        ScortaSottoMinimoEventListener listener =
                new ScortaSottoMinimoEventListener(service);
        ScortaSottoMinimoEvent evento = new ScortaSottoMinimoEvent(
                20L, "P001", "Prodotto", 6, 3, 5,
                "OV-2026-000010");

        listener.gestire(evento);

        verify(service).inviaScortaSottoMinimo(evento);
    }
}
