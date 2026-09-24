package com.gestioneOrdini.exception;

import com.gestioneOrdini.application.assistente.exception.AssistenteNonDisponibileException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void restituisce503QuandoAzureSqlStaRiattivandoIlDatabase() {
        var sqlException = new SQLException(
                "Database 'ProjectJava' is not currently available.", "S0001", 40613);

        var risposta = handler.handleGeneral(new IllegalStateException("Accesso ai dati fallito", sqlException));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, risposta.getStatusCode());
        assertEquals("8", risposta.getHeaders().getFirst("Retry-After"));
        assertEquals("DATABASE_IN_RIATTIVAZIONE", corpo(risposta.getBody()).get("codice"));
    }

    @Test
    void riconosceLaRiattivazioneAncheNelFlussoDellAssistente() {
        var sqlException = new SQLException(
                "Database 'ProjectJava' is not currently available.", "S0001", 40613);
        var eccezione = new AssistenteNonDisponibileException("Dati non disponibili", sqlException);

        var risposta = handler.handleAssistenteNonDisponibile(eccezione);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, risposta.getStatusCode());
        assertEquals("DATABASE_IN_RIATTIVAZIONE", corpo(risposta.getBody()).get("codice"));
    }

    @Test
    void riconosceIlTimeoutTransitorioDelPoolHikariAncheSenzaCodiceAzure() {
        var timeout = new SQLTransientConnectionException(
                "HikariPool-1 - Connection is not available, request timed out after 10000ms");

        var risposta = handler.handleGeneral(new IllegalStateException("Connessione non disponibile", timeout));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, risposta.getStatusCode());
        assertEquals("DATABASE_IN_RIATTIVAZIONE", corpo(risposta.getBody()).get("codice"));
    }

    @Test
    void mantiene500PerUnErroreGenerico() {
        var risposta = handler.handleGeneral(new IllegalStateException("Errore inatteso"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, risposta.getStatusCode());
        assertEquals("Errore interno del server", corpo(risposta.getBody()).get("errore"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> corpo(Object body) {
        return (Map<String, String>) body;
    }
}
