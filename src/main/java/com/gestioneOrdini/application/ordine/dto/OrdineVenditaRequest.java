package com.gestioneOrdini.application.ordine.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record OrdineVenditaRequest(
        @NotNull(message = "Il cliente è obbligatorio") Long clienteId,
        @NotNull(message = "Il venditore è obbligatorio") Long venditoreId,
        @NotNull(message = "Il trasportatore è obbligatorio") Long trasportatoreId,
        LocalDate dataRilascio
) {}
