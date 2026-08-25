package com.gestioneOrdini.application.ordine.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OrdineVenditaResponse(
        Long id,
        String numeroOrdine,
        AgenteRiferimentoResponse cliente,
        AgenteRiferimentoResponse venditore,
        AgenteRiferimentoResponse trasportatore,
        LocalDateTime dataRegistrazione,
        LocalDate dataRilascio,
        LocalDate dataAnnullamento
) {}
