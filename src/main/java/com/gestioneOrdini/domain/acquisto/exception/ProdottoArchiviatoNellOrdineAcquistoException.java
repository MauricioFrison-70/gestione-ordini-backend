package com.gestioneOrdini.domain.acquisto.exception;

public class ProdottoArchiviatoNellOrdineAcquistoException extends RuntimeException {
    public static final String CODICE = "PRODOTTO_ARCHIVIATO_NELL_ORDINE_ACQUISTO";

    public ProdottoArchiviatoNellOrdineAcquistoException(String codiceProdotto) {
        super("Il prodotto " + codiceProdotto
                + " è archiviato e non può essere inserito nell'ordine di acquisto.");
    }
}
