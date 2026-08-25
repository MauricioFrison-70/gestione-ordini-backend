package com.gestioneOrdini.domain.prodotto.event;

/**
 * Evento generato quando la giacenza di un prodotto passa da un valore
 * inferiore alla scorta minima a un valore uguale o superiore alla soglia.
 */
public record ScortaRipristinataEvent(
        Long prodottoId,
        String codiceProdotto,
        String descrizioneProdotto,
        Integer quantitaPrecedente,
        Integer quantitaAttuale,
        Integer scortaMinima,
        String numeroOrdineAcquisto
) {
}
