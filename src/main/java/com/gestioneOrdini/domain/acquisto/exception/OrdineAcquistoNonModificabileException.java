package com.gestioneOrdini.domain.acquisto.exception;

public class OrdineAcquistoNonModificabileException extends RuntimeException {
    public static final String CODICE = "ORDINE_ACQUISTO_NON_MODIFICABILE";

    public OrdineAcquistoNonModificabileException(String numeroOrdine, String motivo) {
        super("L'ordine di acquisto " + numeroOrdine + " non può essere modificato perché " + motivo + ".");
    }
}
