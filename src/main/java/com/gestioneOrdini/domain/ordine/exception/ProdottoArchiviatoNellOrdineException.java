package com.gestioneOrdini.domain.ordine.exception;

public class ProdottoArchiviatoNellOrdineException extends RuntimeException {
    public static final String CODICE = "PRODOTTO_ARCHIVIATO";

    public ProdottoArchiviatoNellOrdineException(String codiceProdotto) {
        super("Il prodotto " + codiceProdotto
                + " è archiviato e non può essere inserito nell'ordine di vendita.");
    }
}
