package com.gestioneOrdini.domain.prodotto.exception;

/**
 * Indica che esiste già un prodotto con lo stesso codice.
 */
public class CodiceProdottoDuplicatoException extends RuntimeException {

    public CodiceProdottoDuplicatoException(String codice, Throwable cause) {
        super("Esiste già un prodotto con il codice '" + codice + "'.", cause);
    }
}
