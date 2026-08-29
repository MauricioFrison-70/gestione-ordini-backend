package com.gestioneOrdini.application.reporting.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record EseguiRapportoRequest(
        @NotNull(message = "I parametri sono obbligatori")
        Map<String, Object> parametri
) {
}
