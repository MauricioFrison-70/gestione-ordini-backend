package com.gestioneOrdini.application.assistente.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DomandaAssistenteRequest(
        @NotBlank(message = "La domanda è obbligatoria")
        @Size(max = 1000, message = "La domanda non può superare 1000 caratteri")
        String domanda,
        @Size(max = 8, message = "La cronologia non può contenere più di 8 messaggi")
        List<@Valid MessaggioConversazioneRequest> cronologia
) {
    public DomandaAssistenteRequest {
        cronologia = cronologia == null ? List.of() : List.copyOf(cronologia);
    }
}
