package com.gestioneOrdini.infrastructure.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestioneOrdini.infrastructure.config.GmailOAuthProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.net.URI;

/**
 * Test reale della Gmail API, eseguito solo quando richiesto esplicitamente.
 * Nessuna credenziale viene memorizzata nel codice sorgente.
 */
@EnabledIfEnvironmentVariable(
        named = "RUN_REAL_EMAIL_TEST",
        matches = "true")
class EmailRealeManualTest {

    @Test
    void dovrebbeInviareUnaEmailRealeConLeVariabiliDiAmbiente() {
        String clientId = obbligatoria("GMAIL_OAUTH_CLIENT_ID");
        String clientSecret = obbligatoria("GMAIL_OAUTH_CLIENT_SECRET");
        String refreshToken = obbligatoria("GMAIL_OAUTH_REFRESH_TOKEN");
        String mittente = obbligatoria("GMAIL_SENDER_EMAIL");
        String destinatario = obbligatoria("EMAIL_RESPONSABILE_SCORTA");

        var properties = new GmailOAuthProperties(
                clientId,
                clientSecret,
                refreshToken,
                URI.create("http://localhost:8081/login/oauth2/code/google"),
                URI.create("https://accounts.google.com/o/oauth2/v2/auth"),
                URI.create("https://oauth2.googleapis.com/token"),
                URI.create("https://gmail.googleapis.com/gmail/v1/"),
                false);
        var transport = new JavaGmailHttpTransport();
        var objectMapper = new ObjectMapper();
        var oauthClient = new GmailOAuthClient(
                transport, objectMapper, properties);
        var emailClient = new GmailApiEmailClient(
                transport, oauthClient, objectMapper, properties);

        emailClient.invia(
                mittente,
                destinatario,
                "[Gestione Ordini] Test notifica scorta",
                "Messaggio di prova del sistema Gestione Ordini.\n\n"
                        + "La Gmail API e l'indirizzo del responsabile "
                        + "della scorta sono operativi.");
    }

    private static String obbligatoria(String nome) {
        String valore = System.getenv(nome);
        if (valore == null || valore.isBlank()) {
            throw new IllegalStateException(
                    "Variabile di ambiente obbligatoria non configurata: " + nome);
        }
        return valore;
    }

}
