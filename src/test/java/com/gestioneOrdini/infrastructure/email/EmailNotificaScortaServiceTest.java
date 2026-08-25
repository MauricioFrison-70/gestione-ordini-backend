package com.gestioneOrdini.infrastructure.email;

import com.gestioneOrdini.domain.prodotto.event.ScortaRipristinataEvent;
import com.gestioneOrdini.domain.prodotto.event.ScortaSottoMinimoEvent;
import com.gestioneOrdini.infrastructure.config.GmailOAuthProperties;
import com.gestioneOrdini.infrastructure.config.NotificaScortaProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailNotificaScortaServiceTest {
    @Mock private GmailApiEmailClient gmailApiEmailClient;

    @Test
    void dovrebbeInviareLaNotificaAlResponsabileDellaScorta() {
        var properties = new NotificaScortaProperties(
                "magazzino@example.com", "sistema@example.com");
        var service = new EmailNotificaScortaService(
                gmailApiEmailClient, properties, oauthProperties());

        service.inviaScortaRipristinata(evento());

        verify(gmailApiEmailClient).invia(
                "sistema@example.com",
                "magazzino@example.com",
                "Scorta del prodotto ripristinata - P001",
                "Il prodotto P001 - Prodotto di prova non è più sotto la scorta minima.\n\n"
                        + "Ordine di acquisto: OA-2026-000001\n"
                        + "Quantità precedente: 1\n"
                        + "Quantità attuale: 8\n"
                        + "Scorta minima: 5");
    }

    @Test
    void dovrebbeInviareLaNotificaQuandoLaVenditaPortaLaScortaSottoIlMinimo() {
        var properties = new NotificaScortaProperties(
                "magazzino@example.com", "sistema@example.com");
        var service = new EmailNotificaScortaService(
                gmailApiEmailClient, properties, oauthProperties());

        service.inviaScortaSottoMinimo(new ScortaSottoMinimoEvent(
                20L, "P001", "Prodotto di prova", 6, 3, 5,
                "OV-2026-000010"));

        verify(gmailApiEmailClient).invia(
                "sistema@example.com",
                "magazzino@example.com",
                "Scorta del prodotto sotto il minimo - P001",
                "Il prodotto P001 - Prodotto di prova è sceso sotto la scorta minima.\n\n"
                        + "Ordine di vendita: OV-2026-000010\n"
                        + "Quantità precedente: 6\n"
                        + "Quantità attuale: 3\n"
                        + "Scorta minima: 5");
    }

    @Test
    void nonDovrebbeTentareLInvioSenzaDestinatarioConfigurato() {
        var properties = new NotificaScortaProperties("", "");
        var service = new EmailNotificaScortaService(
                gmailApiEmailClient, properties, oauthProperties());

        service.inviaScortaRipristinata(evento());

        verify(gmailApiEmailClient, never()).invia(
                anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void nonDovrebbeTentareLInvioSenzaConfigurazioneOauth() {
        var properties = new NotificaScortaProperties(
                "magazzino@example.com", "sistema@example.com");
        var oauthProperties = new GmailOAuthProperties(
                "", "", "", null, null, null, null, false);
        var service = new EmailNotificaScortaService(
                gmailApiEmailClient, properties, oauthProperties);

        service.inviaScortaRipristinata(evento());

        verify(gmailApiEmailClient, never()).invia(
                anyString(), anyString(), anyString(), anyString());
    }

    private GmailOAuthProperties oauthProperties() {
        return new GmailOAuthProperties(
                "client-id",
                "client-secret",
                "refresh-token",
                URI.create("http://localhost:8081/login/oauth2/code/google"),
                URI.create("https://accounts.google.com/o/oauth2/v2/auth"),
                URI.create("https://oauth2.googleapis.com/token"),
                URI.create("https://gmail.googleapis.com/gmail/v1/"),
                false);
    }

    private ScortaRipristinataEvent evento() {
        return new ScortaRipristinataEvent(
                20L,
                "P001",
                "Prodotto di prova",
                1,
                8,
                5,
                "OA-2026-000001");
    }
}
