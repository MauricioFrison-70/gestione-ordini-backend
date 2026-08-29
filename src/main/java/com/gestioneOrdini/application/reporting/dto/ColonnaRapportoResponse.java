package com.gestioneOrdini.application.reporting.dto;

public record ColonnaRapportoResponse(
        String nome,
        String etichetta,
        String tipo,
        String formato,
        boolean totalizzare
) {
}
