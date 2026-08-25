package com.gestioneOrdini.application.acquisto.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OrdineAcquistoResponse(
        Long id,
        String numeroOrdine,
        FornitoreRiferimentoResponse fornitore,
        LocalDateTime dataRegistrazione,
        LocalDate dataRicevimento,
        LocalDate dataAnnullamento
) {}
