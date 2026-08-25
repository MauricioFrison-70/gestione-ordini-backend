package com.gestioneOrdini.infrastructure.email;

import com.gestioneOrdini.application.prodotto.port.NotificaScortaService;
import com.gestioneOrdini.domain.prodotto.event.ScortaRipristinataEvent;
import com.gestioneOrdini.domain.prodotto.event.ScortaSottoMinimoEvent;
import com.gestioneOrdini.infrastructure.config.GmailOAuthProperties;
import com.gestioneOrdini.infrastructure.config.NotificaScortaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificaScortaService implements NotificaScortaService {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(EmailNotificaScortaService.class);

    private final GmailApiEmailClient gmailApiEmailClient;
    private final NotificaScortaProperties properties;
    private final GmailOAuthProperties oauthProperties;

    public EmailNotificaScortaService(
            GmailApiEmailClient gmailApiEmailClient,
            NotificaScortaProperties properties,
            GmailOAuthProperties oauthProperties) {
        this.gmailApiEmailClient = gmailApiEmailClient;
        this.properties = properties;
        this.oauthProperties = oauthProperties;
    }

    @Override
    public void inviaScortaRipristinata(ScortaRipristinataEvent evento) {
        if (!properties.haDestinatario()) {
            LOGGER.warn("Notifica di scorta non inviata: EMAIL_RESPONSABILE_SCORTA non configurata");
            return;
        }
        if (!properties.haMittente()) {
            LOGGER.warn("Notifica di scorta non inviata: GMAIL_SENDER_EMAIL non configurata");
            return;
        }
        if (!oauthProperties.configuratoPerInvio()) {
            LOGGER.warn("Notifica di scorta non inviata: configurazione OAuth Gmail incompleta");
            return;
        }

        try {
            gmailApiEmailClient.invia(
                    properties.mittente(),
                    properties.destinatario(),
                    "Scorta del prodotto ripristinata - "
                            + evento.codiceProdotto(),
                    creaCorpo(evento));
        } catch (GmailApiException ex) {
            LOGGER.error(
                    "Errore durante l'invio della notifica di scorta per il prodotto {}",
                    evento.codiceProdotto(),
                    ex);
        }
    }

    @Override
    public void inviaScortaSottoMinimo(ScortaSottoMinimoEvent evento) {
        if (!configurazioneValida()) {
            return;
        }

        try {
            gmailApiEmailClient.invia(
                    properties.mittente(),
                    properties.destinatario(),
                    "Scorta del prodotto sotto il minimo - " + evento.codiceProdotto(),
                    creaCorpo(evento));
        } catch (GmailApiException ex) {
            LOGGER.error(
                    "Errore durante l'invio della notifica di scorta minima per il prodotto {}",
                    evento.codiceProdotto(),
                    ex);
        }
    }

    private boolean configurazioneValida() {
        if (!properties.haDestinatario()) {
            LOGGER.warn("Notifica di scorta non inviata: EMAIL_RESPONSABILE_SCORTA non configurata");
            return false;
        }
        if (!properties.haMittente()) {
            LOGGER.warn("Notifica di scorta non inviata: GMAIL_SENDER_EMAIL non configurata");
            return false;
        }
        if (!oauthProperties.configuratoPerInvio()) {
            LOGGER.warn("Notifica di scorta non inviata: configurazione OAuth Gmail incompleta");
            return false;
        }
        return true;
    }

    private String creaCorpo(ScortaRipristinataEvent evento) {
        return "Il prodotto " + evento.codiceProdotto() + " - "
                + evento.descrizioneProdotto() + " non è più sotto la scorta minima.\n\n"
                + "Ordine di acquisto: " + evento.numeroOrdineAcquisto() + "\n"
                + "Quantità precedente: " + evento.quantitaPrecedente() + "\n"
                + "Quantità attuale: " + evento.quantitaAttuale() + "\n"
                + "Scorta minima: " + evento.scortaMinima();
    }

    private String creaCorpo(ScortaSottoMinimoEvent evento) {
        return "Il prodotto " + evento.codiceProdotto() + " - "
                + evento.descrizioneProdotto() + " è sceso sotto la scorta minima.\n\n"
                + "Ordine di vendita: " + evento.numeroOrdineVendita() + "\n"
                + "Quantità precedente: " + evento.quantitaPrecedente() + "\n"
                + "Quantità attuale: " + evento.quantitaAttuale() + "\n"
                + "Scorta minima: " + evento.scortaMinima();
    }
}
