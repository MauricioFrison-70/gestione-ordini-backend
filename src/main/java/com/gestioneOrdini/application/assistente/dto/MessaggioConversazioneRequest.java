package com.gestioneOrdini.application.assistente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MessaggioConversazioneRequest(
        @NotNull(message = "Il ruolo è obbligatorio") RuoloMessaggio ruolo,
        @NotBlank(message = "Il contenuto è obbligatorio")
        @Size(max = 1000, message = "Il contenuto non può superare 1000 caratteri")
        String contenuto
) {
    public enum RuoloMessaggio {
        UTENTE,
        ASSISTENTE
    }
}
