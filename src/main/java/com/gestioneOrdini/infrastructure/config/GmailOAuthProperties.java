package com.gestioneOrdini.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "gmail.oauth")
public record GmailOAuthProperties(
        String clientId,
        String clientSecret,
        String refreshToken,
        URI redirectUri,
        URI authorizationUri,
        URI tokenUri,
        URI apiBaseUri,
        boolean setupEnabled
) {
    public boolean configuratoPerInvio() {
        return presente(clientId)
                && presente(clientSecret)
                && presente(refreshToken);
    }

    public boolean configuratoPerAutorizzazione() {
        return presente(clientId)
                && presente(clientSecret)
                && redirectUri != null
                && authorizationUri != null
                && tokenUri != null;
    }

    private boolean presente(String valore) {
        return valore != null && !valore.isBlank();
    }
}
