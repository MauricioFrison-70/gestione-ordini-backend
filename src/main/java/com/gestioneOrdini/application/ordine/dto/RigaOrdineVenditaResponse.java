package com.gestioneOrdini.application.ordine.dto;

import java.math.BigDecimal;

public record RigaOrdineVenditaResponse(
        Long id,
        Long ordineVenditaId,
        String codiceProdotto,
        String descrizioneProdotto,
        Integer quantita,
        BigDecimal valoreUnitario,
        BigDecimal totaleRiga
) {}
