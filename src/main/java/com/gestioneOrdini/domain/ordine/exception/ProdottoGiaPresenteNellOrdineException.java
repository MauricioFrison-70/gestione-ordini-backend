package com.gestioneOrdini.domain.ordine.exception;

public class ProdottoGiaPresenteNellOrdineException extends RuntimeException {
    public static final String CODICE = "PRODOTTO_GIA_PRESENTE_NELL_ORDINE";

    public ProdottoGiaPresenteNellOrdineException(String codiceProdotto, Long ordineId) {
        super("Il prodotto " + codiceProdotto
                + " è già presente nell'ordine di vendita con id " + ordineId + ".");
    }

    public ProdottoGiaPresenteNellOrdineException(
            String codiceProdotto, Long ordineId, Throwable causa) {
        super("Il prodotto " + codiceProdotto
                + " è già presente nell'ordine di vendita con id " + ordineId + ".", causa);
    }
}
