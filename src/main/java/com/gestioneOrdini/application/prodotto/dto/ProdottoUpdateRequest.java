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
 * <p>Il codice e la giacenza non sono presenti perché sono immutabili
 * da questo flusso. La giacenza viene aggiornata esclusivamente dagli
 * ordini di acquisto e di vendita.</p>
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
