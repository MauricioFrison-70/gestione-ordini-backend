package com.gestioneOrdini.infrastructure.email;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public interface GmailHttpTransport {
    HttpResponse<String> invia(HttpRequest richiesta);
}
