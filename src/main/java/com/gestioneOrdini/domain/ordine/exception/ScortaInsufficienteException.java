package com.gestioneOrdini.domain.ordine.exception;

public class ScortaInsufficienteException extends RuntimeException {
    public static final String CODICE = "SCORTA_INSUFFICIENTE";

    public ScortaInsufficienteException(String dettagli) {
        super("Scorta insufficiente. L'operazione è stata annullata. " + dettagli);
    }
}
