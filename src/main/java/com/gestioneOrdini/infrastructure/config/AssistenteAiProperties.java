package com.gestioneOrdini.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "assistente.ai")
public record AssistenteAiProperties(
        boolean enabled,
        URI endpoint,
        String apiKey,
        String model,
        int timeoutSeconds,
        int maxCompletionTokens
) {
    public boolean configurato() {
        return enabled
                && endpoint != null
                && presente(apiKey)
                && presente(model)
                && timeoutSeconds > 0
                && maxCompletionTokens > 0;
    }

    private boolean presente(String valore) {
        return valore != null && !valore.isBlank();
    }
}
