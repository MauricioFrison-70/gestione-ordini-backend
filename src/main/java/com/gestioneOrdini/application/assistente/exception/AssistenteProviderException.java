package com.gestioneOrdini.application.assistente.exception;

public class AssistenteProviderException extends RuntimeException {
    public AssistenteProviderException(String message) {
        super(message);
    }

    public AssistenteProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
