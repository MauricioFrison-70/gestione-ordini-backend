package com.gestioneOrdini.application.assistente.exception;

public class AssistenteNonDisponibileException extends RuntimeException {
    public AssistenteNonDisponibileException(String message) {
        super(message);
    }

    public AssistenteNonDisponibileException(String message, Throwable cause) {
        super(message, cause);
    }
}
