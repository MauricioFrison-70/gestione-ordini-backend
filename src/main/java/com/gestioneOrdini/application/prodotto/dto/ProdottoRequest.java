package com.gestioneOrdini.application.prodotto.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO di input utilizzato per creare o aggiornare un prodotto.
 *
 * <p>Contiene esclusivamente i campi che il client può fornire
 * nell'ambito di una richiesta REST. I campi generati dal sistema
 * (come l'identificativo o la data di registrazione) non sono inclusi.</p>
 *
 * <p>Il campo {@code archiviato} è opzionale: se non specificato,
 * viene impostato automaticamente a {@code false}.</p>
 */
public record ProdottoRequest(

        @NotBlank(message = "Il codice è obbligatorio")
        @Size(max = 6, message = "Il codice non può superare 6 caratteri")
        String codice,

        @NotBlank(message = "La descrizione è obbligatoria")
        @Size(max = 30, message = "La descrizione non può superare 30 caratteri")
        String descrizione,

        @NotNull(message = "Il valore di acquisto è obbligatorio")
        @DecimalMin(value = "0.00", message = "Il valore di acquisto deve essere positivo")
        BigDecimal valoreAcquisto,

        @NotNull(message = "Il valore di vendita è obbligatorio")
        @DecimalMin(value = "0.00", message = "Il valore di vendita deve essere positivo")
        BigDecimal valoreVendita,

        @NotNull(message = "La quantità è obbligatoria")
        @Min(value = 0, message = "La quantità deve essere maggiore o uguale a zero")
        Integer quantita,

        @NotNull(message = "La scorta minima è obbligatoria")
        @Min(value = 0, message = "La scorta minima deve essere maggiore o uguale a zero")
        Integer scortaMinima,

        Boolean archiviato
) {
    public ProdottoRequest {
        if (archiviato == null) {
            archiviato = false;
        }
    }
}
