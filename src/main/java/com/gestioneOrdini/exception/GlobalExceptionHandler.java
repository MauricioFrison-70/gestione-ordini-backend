package com.gestioneOrdini.exception;


import com.gestioneOrdini.domain.agente.exception.AgenteUtilizzatoException;
import com.gestioneOrdini.domain.ordine.exception.OrdineVenditaRilasciatoException;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import com.gestioneOrdini.domain.prodotto.exception.CodiceProdottoDuplicatoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Gestore globale delle eccezioni dell'applicazione.
 * Fornisce risposte coerenti, sicure e adatte a un'API REST moderna.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Errori di validazione generati da @Valid.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {

        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        err -> Optional.ofNullable(err.getDefaultMessage()).orElse("Messaggio non disponibile"),
                        (a, b) -> a
                ));

        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Eccezioni di dominio (DDD), come entità non trovata.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("errore", ex.getMessage()));
    }

    /**
     * Codice prodotto già presente. Il dettaglio tecnico del vincolo di database
     * non viene esposto al client.
     */
    @ExceptionHandler(CodiceProdottoDuplicatoException.class)
    public ResponseEntity<?> handleCodiceProdottoDuplicato(CodiceProdottoDuplicatoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("errore", ex.getMessage()));
    }

    /** Violazioni delle regole di dominio inviate dal client. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("errore", ex.getMessage()));
    }

    @ExceptionHandler(AgenteUtilizzatoException.class)
    public ResponseEntity<?> handleAgenteUtilizzato(AgenteUtilizzatoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "codice", AgenteUtilizzatoException.CODICE,
                "errore", ex.getMessage()
        ));
    }

    @ExceptionHandler(OrdineVenditaRilasciatoException.class)
    public ResponseEntity<?> handleOrdineVenditaRilasciato(OrdineVenditaRilasciatoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "codice", OrdineVenditaRilasciatoException.CODICE,
                "errore", ex.getMessage()
        ));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleMetodoNonSupportato(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(Map.of(
                "errore", "Metodo HTTP non supportato per questa risorsa"
        ));
    }

    /**
     * Errori generici non previsti.
     * Non espone dettagli sensibili al client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneral(Exception ex) {
        log.error("Errore interno del server", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("errore", "Errore interno del server"));
    }

}
