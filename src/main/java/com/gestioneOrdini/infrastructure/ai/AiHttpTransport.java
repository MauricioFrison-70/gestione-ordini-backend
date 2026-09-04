package com.gestioneOrdini.infrastructure.ai;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public interface AiHttpTransport {
    HttpResponse<String> invia(HttpRequest richiesta);
}
