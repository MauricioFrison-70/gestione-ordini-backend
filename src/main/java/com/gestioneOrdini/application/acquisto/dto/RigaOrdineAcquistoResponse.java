package com.gestioneOrdini.application.acquisto.dto;

import java.math.BigDecimal;

public record RigaOrdineAcquistoResponse(
        Long id,
        Long ordineAcquistoId,
        String codiceProdotto,
        String descrizioneProdotto,
        Integer quantita,
        BigDecimal valoreUnitario,
        BigDecimal totaleRiga
) {}
