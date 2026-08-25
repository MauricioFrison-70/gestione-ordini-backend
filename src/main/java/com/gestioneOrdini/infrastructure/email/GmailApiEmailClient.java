package com.gestioneOrdini.infrastructure.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestioneOrdini.infrastructure.config.GmailOAuthProperties;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

@Component
public class GmailApiEmailClient {
    private final GmailHttpTransport httpTransport;
    private final GmailOAuthClient oauthClient;
    private final ObjectMapper objectMapper;
    private final GmailOAuthProperties properties;

    public GmailApiEmailClient(
            GmailHttpTransport httpTransport,
            GmailOAuthClient oauthClient,
            ObjectMapper objectMapper,
            GmailOAuthProperties properties) {
        this.httpTransport = httpTransport;
        this.oauthClient = oauthClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public void invia(
            String mittente,
            String destinatario,
            String oggetto,
            String corpo) {
        String accessToken = oauthClient.ottieniAccessToken();
        String messaggioRaw = creaMessaggioRaw(
                mittente, destinatario, oggetto, corpo);

        HttpRequest richiesta = HttpRequest.newBuilder(uriInvio())
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        creaCorpoJson(messaggioRaw)))
                .build();

        HttpResponse<String> risposta = httpTransport.invia(richiesta);
        if (risposta.statusCode() < 200 || risposta.statusCode() >= 300) {
            throw new GmailApiException(
                    "Gmail API ha rifiutato l'invio (HTTP "
                            + risposta.statusCode() + ")");
        }
    }

    private URI uriInvio() {
        if (properties.apiBaseUri() == null) {
            throw new GmailApiException(
                    "URI della Gmail API non configurato");
        }
        return properties.apiBaseUri().resolve("users/me/messages/send");
    }

    private String creaMessaggioRaw(
            String mittente,
            String destinatario,
            String oggetto,
            String corpo) {
        String oggettoCodificato = Base64.getEncoder().encodeToString(
                oggetto.getBytes(StandardCharsets.UTF_8));
        String corpoCodificato = Base64.getMimeEncoder(76, new byte[]{'\r', '\n'})
                .encodeToString(corpo.getBytes(StandardCharsets.UTF_8));

        String messaggio = "From: " + intestazioneSicura(mittente) + "\r\n"
                + "To: " + intestazioneSicura(destinatario) + "\r\n"
                + "Subject: =?UTF-8?B?" + oggettoCodificato + "?=\r\n"
                + "MIME-Version: 1.0\r\n"
                + "Content-Type: text/plain; charset=UTF-8\r\n"
                + "Content-Transfer-Encoding: base64\r\n"
                + "\r\n"
                + corpoCodificato;

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(messaggio.getBytes(StandardCharsets.UTF_8));
    }

    private String intestazioneSicura(String valore) {
        if (valore == null || valore.isBlank()
                || valore.contains("\r") || valore.contains("\n")) {
            throw new GmailApiException(
                    "Indirizzo e-mail non valido");
        }
        return valore;
    }

    private String creaCorpoJson(String messaggioRaw) {
        try {
            return objectMapper.writeValueAsString(Map.of("raw", messaggioRaw));
        } catch (JsonProcessingException ex) {
            throw new GmailApiException(
                    "Impossibile preparare il messaggio Gmail", ex);
        }
    }
}
