package com.gestioneOrdini.domain.prodotto.event;

/** Evento generato quando una vendita porta la giacenza sotto la scorta minima. */
public record ScortaSottoMinimoEvent(
        Long prodottoId,
        String codiceProdotto,
        String descrizioneProdotto,
        Integer quantitaPrecedente,
        Integer quantitaAttuale,
        Integer scortaMinima,
        String numeroOrdineVendita
) {
}
