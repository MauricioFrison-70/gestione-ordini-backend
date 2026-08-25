package com.gestioneOrdini.infrastructure.event.handler;

import com.gestioneOrdini.application.prodotto.port.NotificaScortaService;
import com.gestioneOrdini.domain.prodotto.event.ScortaRipristinataEvent;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ScortaRipristinataEventListenerTest {

    @Test
    void dovrebbeDelegareLaNotificaAlServizioConfigurato() {
        NotificaScortaService service = mock(NotificaScortaService.class);
        ScortaRipristinataEventListener listener =
                new ScortaRipristinataEventListener(service);
        ScortaRipristinataEvent evento = new ScortaRipristinataEvent(
                20L, "P001", "Prodotto", 1, 8, 5,
                "OA-2026-000001");

        listener.gestire(evento);

        verify(service).inviaScortaRipristinata(evento);
    }
}
