package com.gestioneOrdini.domain.ordine.exception;

public class OrdineVenditaAnnullatoException extends RuntimeException {
    public static final String CODICE = "ORDINE_VENDITA_ANNULLATO";

    public OrdineVenditaAnnullatoException(String numeroOrdine) {
        super("L'ordine di vendita " + numeroOrdine
                + " non può essere eliminato perché ha già una data di annullamento.");
    }
}
