package com.gestioneOrdini.domain.ordine.exception;

public class OrdineVenditaNonModificabileException extends RuntimeException {
    public static final String CODICE = "ORDINE_VENDITA_NON_MODIFICABILE";

    public OrdineVenditaNonModificabileException(String numeroOrdine, String motivo) {
        super("L'ordine di vendita " + numeroOrdine
                + " non può essere modificato perché " + motivo + ".");
    }
}
