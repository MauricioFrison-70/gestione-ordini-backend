package com.gestioneOrdini.application.acquisto.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RigaOrdineAcquistoRequest(
        @NotBlank(message = "Il codice del prodotto è obbligatorio")
        String codiceProdotto,

        @NotNull(message = "La quantità è obbligatoria")
        @Positive(message = "La quantità deve essere maggiore di zero")
        @Max(value = 999_999_999, message = "La quantità non può superare 999999999")
        Integer quantita,

        @NotNull(message = "Il valore unitario è obbligatorio")
        @DecimalMin(value = "0.00", message = "Il valore unitario deve essere maggiore o uguale a zero")
        @Digits(integer = 13, fraction = 2, message = "Il valore unitario deve avere al massimo 13 interi e 2 decimali")
        BigDecimal valoreUnitario
) {}
