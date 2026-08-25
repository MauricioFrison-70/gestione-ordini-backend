package com.gestioneOrdini.domain.ordine.exception;

public class OrdineVenditaRilasciatoException extends RuntimeException {
    public static final String CODICE = "ORDINE_VENDITA_RILASCIATO";

    public OrdineVenditaRilasciatoException(String numeroOrdine) {
        super("L'ordine di vendita " + numeroOrdine
                + " non può essere eliminato perché ha già una data di rilascio.");
    }
}
