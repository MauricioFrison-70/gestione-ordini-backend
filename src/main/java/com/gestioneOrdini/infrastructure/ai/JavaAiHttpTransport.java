package com.gestioneOrdini.infrastructure.ai;

import com.gestioneOrdini.application.assistente.exception.AssistenteNonDisponibileException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class JavaAiHttpTransport implements AiHttpTransport {
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    @Override
    public HttpResponse<String> invia(HttpRequest richiesta) {
        try {
            return httpClient.send(richiesta, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AssistenteNonDisponibileException("Richiesta al provider IA interrotta", ex);
        } catch (IOException ex) {
            throw new AssistenteNonDisponibileException("Provider IA non raggiungibile", ex);
        }
    }
}
