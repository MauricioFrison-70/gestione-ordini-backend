package com.gestioneOrdini.domain.acquisto.exception;

public class ProdottoGiaPresenteNellOrdineAcquistoException extends RuntimeException {
    public static final String CODICE = "PRODOTTO_GIA_PRESENTE_NELL_ORDINE_ACQUISTO";

    public ProdottoGiaPresenteNellOrdineAcquistoException(
            String codiceProdotto, Long ordineId) {
        super("Il prodotto " + codiceProdotto
                + " è già presente nell'ordine di acquisto con id " + ordineId + ".");
    }

    public ProdottoGiaPresenteNellOrdineAcquistoException(
            String codiceProdotto, Long ordineId, Throwable causa) {
        super("Il prodotto " + codiceProdotto
                + " è già presente nell'ordine di acquisto con id " + ordineId + ".", causa);
    }
}
