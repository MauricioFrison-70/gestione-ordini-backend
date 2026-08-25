package com.gestioneOrdini.application.ordine.dto;

import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RigaOrdineVenditaRequest(
        @NotBlank(message = "Il codice prodotto è obbligatorio")
        @Size(max = Prodotto.LUNGHEZZA_MASSIMA_CODICE,
                message = "Il codice prodotto non può superare 6 caratteri")
        String codiceProdotto,

        @NotNull(message = "La quantità è obbligatoria")
        @Positive(message = "La quantità deve essere maggiore di zero")
        @Max(value = 999_999_999, message = "La quantità non può superare 999999999")
        Integer quantita,

        @NotNull(message = "Il valore unitario è obbligatorio")
        @PositiveOrZero(message = "Il valore unitario deve essere maggiore o uguale a zero")
        @Digits(integer = 13, fraction = 2,
                message = "Il valore unitario deve avere al massimo 13 cifre intere e 2 decimali")
        BigDecimal valoreUnitario
) {}
