package com.gestioneOrdini.application.reporting.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record EsecuzioneRapportoResponse(
        List<ColonnaRapportoResponse> colonne,
        List<Map<String, Object>> righe,
        Map<String, BigDecimal> totali,
        int totaleRighe,
        boolean troncato
) {
}
