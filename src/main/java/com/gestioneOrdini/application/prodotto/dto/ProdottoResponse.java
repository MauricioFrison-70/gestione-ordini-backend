package com.gestioneOrdini.application.prodotto.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO di output che rappresenta un prodotto restituito dalle API.
 *
 * <p>Contiene le informazioni principali del prodotto così come
 * registrate nel sistema, incluse le proprietà di stato e i valori
 * economici rilevanti.</p>
 */
public record ProdottoResponse(
        Long id,
        String codice,
        String descrizione,
        BigDecimal valoreAcquisto,
        BigDecimal valoreVendita,
        Integer quantita,
        Integer scortaMinima,
        Boolean archiviato,
        LocalDateTime dataRegistrazione
) {
}
