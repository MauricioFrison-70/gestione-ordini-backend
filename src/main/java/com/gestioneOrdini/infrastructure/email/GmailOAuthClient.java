package com.gestioneOrdini.infrastructure.email;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestioneOrdini.infrastructure.config.GmailOAuthProperties;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class GmailOAuthClient {
    private static final String GMAIL_SEND_SCOPE =
            "https://www.googleapis.com/auth/gmail.send";

    private final GmailHttpTransport httpTransport;
    private final ObjectMapper objectMapper;
    private final GmailOAuthProperties properties;

    private String accessTokenInCache;
    private Instant scadenzaAccessToken = Instant.EPOCH;

    public GmailOAuthClient(
            GmailHttpTransport httpTransport,
            ObjectMapper objectMapper,
            GmailOAuthProperties properties) {
        this.httpTransport = httpTransport;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public synchronized String ottieniAccessToken() {
        if (accessTokenInCache != null
                && Instant.now().isBefore(scadenzaAccessToken.minusSeconds(60))) {
            return accessTokenInCache;
        }
        if (!properties.configuratoPerInvio()) {
            throw new GmailApiException(
                    "Configurazione OAuth Gmail incompleta");
        }

        Map<String, String> parametri = new LinkedHashMap<>();
        parametri.put("client_id", properties.clientId());
        parametri.put("client_secret", properties.clientSecret());
        parametri.put("refresh_token", properties.refreshToken());
        parametri.put("grant_type", "refresh_token");

        JsonNode risposta = richiediToken(parametri);
        accessTokenInCache = campoObbligatorio(risposta, "access_token");
        long durataSecondi = risposta.path("expires_in").asLong(3600);
        scadenzaAccessToken = Instant.now().plusSeconds(durataSecondi);
        return accessTokenInCache;
    }

    public URI creaUrlAutorizzazione(String stato) {
        if (!properties.configuratoPerAutorizzazione()) {
            throw new GmailApiException(
                    "Configurazione OAuth Gmail incompleta per l'autorizzazione");
        }

        Map<String, String> parametri = new LinkedHashMap<>();
        parametri.put("client_id", properties.clientId());
        parametri.put("redirect_uri", properties.redirectUri().toString());
        parametri.put("response_type", "code");
        parametri.put("scope", GMAIL_SEND_SCOPE);
        parametri.put("access_type", "offline");
        parametri.put("prompt", "consent");
        parametri.put("include_granted_scopes", "true");
        parametri.put("state", stato);

        return URI.create(properties.authorizationUri()
                + "?" + codificaParametri(parametri));
    }

    public String scambiaCodicePerRefreshToken(String codice) {
        if (!properties.configuratoPerAutorizzazione()) {
            throw new GmailApiException(
                    "Configurazione OAuth Gmail incompleta per l'autorizzazione");
        }

        Map<String, String> parametri = new LinkedHashMap<>();
        parametri.put("client_id", properties.clientId());
        parametri.put("client_secret", properties.clientSecret());
        parametri.put("code", codice);
        parametri.put("redirect_uri", properties.redirectUri().toString());
        parametri.put("grant_type", "authorization_code");

        JsonNode risposta = richiediToken(parametri);
        return campoObbligatorio(risposta, "refresh_token");
    }

    private JsonNode richiediToken(Map<String, String> parametri) {
        HttpRequest richiesta = HttpRequest.newBuilder(properties.tokenUri())
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        codificaParametri(parametri)))
                .build();

        HttpResponse<String> risposta = httpTransport.invia(richiesta);
        if (risposta.statusCode() < 200 || risposta.statusCode() >= 300) {
            throw new GmailApiException(
                    "Google OAuth ha rifiutato la richiesta (HTTP "
                            + risposta.statusCode() + ")");
        }

        try {
            return objectMapper.readTree(risposta.body());
        } catch (Exception ex) {
            throw new GmailApiException(
                    "Risposta OAuth Google non valida", ex);
        }
    }

    private String campoObbligatorio(JsonNode risposta, String nomeCampo) {
        String valore = risposta.path(nomeCampo).asText();
        if (valore.isBlank()) {
            throw new GmailApiException(
                    "La risposta OAuth non contiene " + nomeCampo);
        }
        return valore;
    }

    private String codificaParametri(Map<String, String> parametri) {
        return parametri.entrySet().stream()
                .map(entry -> codifica(entry.getKey())
                        + "=" + codifica(entry.getValue()))
                .reduce((primo, secondo) -> primo + "&" + secondo)
                .orElse("");
    }

    private String codifica(String valore) {
        return URLEncoder.encode(valore, StandardCharsets.UTF_8);
    }
}
