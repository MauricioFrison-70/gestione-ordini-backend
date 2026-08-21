package com.gestioneOrdini.application.prodotto.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO di input per l'aggiornamento di un prodotto.
 *
 * <p>Il codice non è presente perché, una volta creato il prodotto,
 * costituisce un identificatore di business immutabile.</p>
 */
public record ProdottoUpdateRequest(

        @NotBlank(message = "La descrizione è obbligatoria")
        @Size(max = 30, message = "La descrizione non può superare 30 caratteri")
        String descrizione,

        @NotNull(message = "Il valore di acquisto è obbligatorio")
        @DecimalMin(value = "0.00", message = "Il valore di acquisto deve essere maggiore o uguale a zero")
        BigDecimal valoreAcquisto,

        @NotNull(message = "Il valore di vendita è obbligatorio")
        @DecimalMin(value = "0.00", message = "Il valore di vendita deve essere maggiore o uguale a zero")
        BigDecimal valoreVendita,

        @NotNull(message = "La quantità è obbligatoria")
        @Min(value = 0, message = "La quantità deve essere maggiore o uguale a zero")
        Integer quantita,

        @NotNull(message = "La scorta minima è obbligatoria")
        @Min(value = 0, message = "La scorta minima deve essere maggiore o uguale a zero")
        Integer scortaMinima,

        Boolean archiviato
) {
    public ProdottoUpdateRequest {
        if (archiviato == null) {
            archiviato = false;
        }
    }
}
