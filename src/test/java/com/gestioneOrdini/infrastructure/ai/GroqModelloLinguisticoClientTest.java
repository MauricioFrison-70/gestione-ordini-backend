package com.gestioneOrdini.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestioneOrdini.application.assistente.exception.AssistenteNonDisponibileException;
import com.gestioneOrdini.application.assistente.port.ModelloLinguisticoClient;
import com.gestioneOrdini.infrastructure.config.AssistenteAiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Flow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroqModelloLinguisticoClientTest {
    @Mock AiHttpTransport transport;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void dovrebbeInviareUnaRichiestaStrutturataSenzaEsporreLaChiaveNelCorpo() throws Exception {
        var client = new GroqModelloLinguisticoClient(transport, objectMapper, properties(true));
        HttpResponse<String> rispostaHttp = response(200,
                "{\"choices\":[{\"message\":{\"content\":\"{\\\"inAmbito\\\":true,"
                        + "\\\"risposta\\\":\\\"Solo gli ordini pendenti.\\\"}\"}}]}");
        when(transport.invia(any(HttpRequest.class))).thenReturn(rispostaHttp);

        var risultato = client.rispondi(
                "Istruzioni e conoscenza",
                List.of(new ModelloLinguisticoClient.MessaggioModello("user", "Prima domanda")),
                "Posso modificare un ordine?");

        assertThat(risultato.inAmbito()).isTrue();
        assertThat(risultato.risposta()).isEqualTo("Solo gli ordini pendenti.");

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(transport).invia(captor.capture());
        HttpRequest request = captor.getValue();
        assertThat(request.uri()).isEqualTo(URI.create(
                "https://api.groq.com/openai/v1/chat/completions"));
        assertThat(request.headers().firstValue("Authorization")).contains("Bearer chiave-segreta");
        JsonNode json = objectMapper.readTree(corpo(request));
        assertThat(json.path("model").asText()).isEqualTo("openai/gpt-oss-20b");
        assertThat(json.path("response_format").path("type").asText()).isEqualTo("json_schema");
        assertThat(json.path("messages").size()).isEqualTo(3);
        assertThat(corpo(request)).doesNotContain("chiave-segreta");
    }

    @Test
    void dovrebbeRestareDisabilitatoSenzaConfigurazione() {
        var client = new GroqModelloLinguisticoClient(transport, objectMapper, properties(false));

        assertThatThrownBy(() -> client.rispondi("istruzioni", List.of(), "domanda"))
                .isInstanceOf(AssistenteNonDisponibileException.class);
    }

    private AssistenteAiProperties properties(boolean enabled) {
        return new AssistenteAiProperties(
                enabled,
                URI.create("https://api.groq.com/openai/v1/chat/completions"),
                "chiave-segreta",
                "openai/gpt-oss-20b",
                30,
                600);
    }

    private String corpo(HttpRequest request) {
        var subscriber = new BodySubscriber();
        request.bodyPublisher().orElseThrow().subscribe(subscriber);
        return subscriber.body();
    }

    @SuppressWarnings("unchecked")
    private HttpResponse<String> response(int status, String body) {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(status);
        when(response.body()).thenReturn(body);
        return response;
    }

    private static class BodySubscriber implements Flow.Subscriber<ByteBuffer> {
        private final StringBuilder body = new StringBuilder();

        @Override public void onSubscribe(Flow.Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }
        @Override public void onNext(ByteBuffer item) {
            byte[] bytes = new byte[item.remaining()];
            item.get(bytes);
            body.append(new String(bytes, StandardCharsets.UTF_8));
        }
        @Override public void onError(Throwable throwable) {
            throw new AssertionError(throwable);
        }
        @Override public void onComplete() { }
        String body() { return body.toString(); }
    }
}
