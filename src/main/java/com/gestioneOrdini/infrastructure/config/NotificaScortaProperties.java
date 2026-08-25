package com.gestioneOrdini.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notifiche.scorta")
public record NotificaScortaProperties(
        String destinatario,
        String mittente
) {
    public boolean haDestinatario() {
        return destinatario != null && !destinatario.isBlank();
    }

    public boolean haMittente() {
        return mittente != null && !mittente.isBlank();
    }
}
