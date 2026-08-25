package com.gestioneOrdini.infrastructure.email;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class JavaGmailHttpTransport implements GmailHttpTransport {
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    @Override
    public HttpResponse<String> invia(HttpRequest richiesta) {
        try {
            return httpClient.send(
                    richiesta,
                    HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new GmailApiException(
                    "Richiesta alla Gmail API interrotta", ex);
        } catch (IOException ex) {
            throw new GmailApiException(
                    "Errore di comunicazione con la Gmail API", ex);
        }
    }
}
