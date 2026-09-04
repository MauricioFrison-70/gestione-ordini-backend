package com.gestioneOrdini.infrastructure.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestioneOrdini.application.assistente.exception.AssistenteNonDisponibileException;
import com.gestioneOrdini.application.assistente.exception.AssistenteProviderException;
import com.gestioneOrdini.application.assistente.port.ModelloLinguisticoClient;
import com.gestioneOrdini.infrastructure.config.AssistenteAiProperties;
import org.springframework.stereotype.Component;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class GroqModelloLinguisticoClient implements ModelloLinguisticoClient {
    private final AiHttpTransport transport;
    private final ObjectMapper objectMapper;
    private final AssistenteAiProperties properties;

    public GroqModelloLinguisticoClient(
            AiHttpTransport transport,
            ObjectMapper objectMapper,
            AssistenteAiProperties properties) {
        this.transport = transport;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public ValutazioneRisposta rispondi(
            String istruzioni,
            List<MessaggioModello> cronologia,
            String domanda) {
        if (!properties.configurato()) {
            throw new AssistenteNonDisponibileException(
                    "L'assistente IA non è configurato o non è abilitato");
        }

        HttpRequest richiesta = HttpRequest.newBuilder(properties.endpoint())
                .timeout(Duration.ofSeconds(properties.timeoutSeconds()))
                .header("Authorization", "Bearer " + properties.apiKey())
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(creareCorpo(istruzioni, cronologia, domanda)))
                .build();

        HttpResponse<String> risposta = transport.invia(richiesta);
        if (risposta.statusCode() == 429 || risposta.statusCode() >= 500) {
            throw new AssistenteNonDisponibileException(
                    "Provider IA temporaneamente non disponibile (HTTP " + risposta.statusCode() + ")");
        }
        if (risposta.statusCode() < 200 || risposta.statusCode() >= 300) {
            throw new AssistenteProviderException(
                    "Il provider IA ha rifiutato la richiesta (HTTP " + risposta.statusCode() + ")");
        }
        return leggereRisposta(risposta.body());
    }

    private String creareCorpo(
            String istruzioni,
            List<MessaggioModello> cronologia,
            String domanda) {
        List<Map<String, String>> messaggi = new ArrayList<>();
        messaggi.add(Map.of("role", "system", "content", istruzioni));
        cronologia.forEach(messaggio -> messaggi.add(Map.of(
                "role", messaggio.ruolo(),
                "content", messaggio.contenuto())));
        messaggi.add(Map.of("role", "user", "content", domanda));

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of(
                "inAmbito", Map.of("type", "boolean"),
                "risposta", Map.of("type", "string")));
        schema.put("required", List.of("inAmbito", "risposta"));
        schema.put("additionalProperties", false);

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("model", properties.model());
        corpo.put("messages", messaggi);
        corpo.put("temperature", 0.1);
        corpo.put("max_completion_tokens", properties.maxCompletionTokens());
        corpo.put("response_format", Map.of(
                "type", "json_schema",
                "json_schema", Map.of(
                        "name", "risposta_assistente",
                        "strict", true,
                        "schema", schema)));
        try {
            return objectMapper.writeValueAsString(corpo);
        } catch (JsonProcessingException ex) {
            throw new AssistenteProviderException("Impossibile preparare la richiesta IA", ex);
        }
    }

    private ValutazioneRisposta leggereRisposta(String corpo) {
        try {
            JsonNode radice = objectMapper.readTree(corpo);
            String contenuto = radice.path("choices").path(0)
                    .path("message").path("content").asText(null);
            if (contenuto == null) {
                throw new AssistenteProviderException("Risposta IA priva di contenuto");
            }
            JsonNode risultato = objectMapper.readTree(contenuto);
            if (!risultato.has("inAmbito") || !risultato.has("risposta")) {
                throw new AssistenteProviderException("Risposta IA non conforme al formato previsto");
            }
            return new ValutazioneRisposta(
                    risultato.path("inAmbito").asBoolean(),
                    risultato.path("risposta").asText());
        } catch (JsonProcessingException ex) {
            throw new AssistenteProviderException("Risposta IA non valida", ex);
        }
    }
}
