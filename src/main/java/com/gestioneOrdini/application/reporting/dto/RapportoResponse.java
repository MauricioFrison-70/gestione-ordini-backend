package com.gestioneOrdini.application.reporting.dto;

import com.gestioneOrdini.domain.reporting.model.Rapporto;

import java.util.List;

public record RapportoResponse(
        Long id,
        String codice,
        String titolo,
        String descrizione,
        boolean attivo,
        List<ParametroRapportoResponse> parametri
) {
    public static RapportoResponse from(Rapporto rapporto) {
        return new RapportoResponse(rapporto.getId(), rapporto.getCodice(), rapporto.getTitolo(),
                rapporto.getDescrizione(), rapporto.isAttivo(), rapporto.getParametri().stream()
                .map(ParametroRapportoResponse::from).toList());
    }
}
