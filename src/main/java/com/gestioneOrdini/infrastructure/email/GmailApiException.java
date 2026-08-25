package com.gestioneOrdini.infrastructure.email;

public class GmailApiException extends RuntimeException {
    public GmailApiException(String message) {
        super(message);
    }

    public GmailApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
