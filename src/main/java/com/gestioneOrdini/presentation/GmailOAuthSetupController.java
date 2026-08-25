package com.gestioneOrdini.presentation;

import com.gestioneOrdini.infrastructure.email.GmailApiException;
import com.gestioneOrdini.infrastructure.email.GmailOAuthClient;
import jakarta.servlet.http.HttpSession;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.security.SecureRandom;
import java.util.Base64;

@RestController
@ConditionalOnProperty(
        name = "gmail.oauth.setup-enabled",
        havingValue = "true")
public class GmailOAuthSetupController {
    static final String SESSION_STATE = "gmail-oauth-state";

    private final GmailOAuthClient oauthClient;
    private final SecureRandom secureRandom = new SecureRandom();

    public GmailOAuthSetupController(GmailOAuthClient oauthClient) {
        this.oauthClient = oauthClient;
    }

    @GetMapping("/api/setup/gmail/oauth/authorize")
    public ResponseEntity<Void> autorizza(HttpSession sessione) {
        String stato = nuovoStato();
        sessione.setAttribute(SESSION_STATE, stato);
        URI destinazione = oauthClient.creaUrlAutorizzazione(stato);
        return ResponseEntity.status(302)
                .location(destinazione)
                .build();
    }

    @GetMapping("/login/oauth2/code/google")
    public ResponseEntity<String> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpSession sessione) {
        if (error != null) {
            return rispostaErrore(
                    "Autorizzazione Google annullata o rifiutata: " + error);
        }

        Object statoAtteso = sessione.getAttribute(SESSION_STATE);
        sessione.removeAttribute(SESSION_STATE);
        if (statoAtteso == null || !statoAtteso.equals(state)) {
            return rispostaErrore(
                    "Stato OAuth non valido. Riavviare l'autorizzazione.");
        }
        if (code == null || code.isBlank()) {
            return rispostaErrore(
                    "Google non ha restituito il codice di autorizzazione.");
        }

        try {
            String refreshToken = oauthClient
                    .scambiaCodicePerRefreshToken(code);
            return rispostaSenzaCache(
                    200,
                    "Autorizzazione completata.\n\n"
                            + "Creare la seguente variabile di ambiente:\n\n"
                            + "GMAIL_OAUTH_REFRESH_TOKEN=" + refreshToken + "\n\n"
                            + "Dopo averla salvata, disattivare "
                            + "GMAIL_OAUTH_SETUP_ENABLED e riavviare il backend.");
        } catch (GmailApiException ex) {
            return rispostaErrore(ex.getMessage());
        }
    }

    private String nuovoStato() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private ResponseEntity<String> rispostaErrore(String messaggio) {
        return rispostaSenzaCache(400, messaggio);
    }

    private ResponseEntity<String> rispostaSenzaCache(
            int status,
            String contenuto) {
        return ResponseEntity.status(status)
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header("Referrer-Policy", "no-referrer")
                .contentType(new MediaType("text", "plain"))
                .body(contenuto);
    }
}
