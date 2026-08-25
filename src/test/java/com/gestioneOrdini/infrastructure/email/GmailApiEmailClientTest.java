package com.gestioneOrdini.infrastructure.email;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestioneOrdini.infrastructure.config.GmailOAuthProperties;
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
import java.util.Base64;
import java.util.concurrent.Flow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GmailApiEmailClientTest {
    @Mock private GmailHttpTransport transport;
    @Mock private GmailOAuthClient oauthClient;

    private ObjectMapper objectMapper;
    private GmailApiEmailClient client;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        client = new GmailApiEmailClient(
                transport, oauthClient, objectMapper, properties());
    }

    @Test
    void dovrebbeInviareUnMessaggioMimeTramiteGmailApi() throws Exception {
        when(oauthClient.ottieniAccessToken()).thenReturn("access-123");
        HttpResponse<String> response = response(200);
        when(transport.invia(org.mockito.ArgumentMatchers
                .any(HttpRequest.class))).thenReturn(response);

        client.invia(
                "sistema@example.com",
                "magazzino@example.com",
                "Scorta ripristinata",
                "Quantità attuale: 8");

        ArgumentCaptor<HttpRequest> captor =
                ArgumentCaptor.forClass(HttpRequest.class);
        verify(transport).invia(captor.capture());
        HttpRequest request = captor.getValue();
        assertThat(request.uri()).isEqualTo(URI.create(
                "https://gmail.googleapis.com/gmail/v1/users/me/messages/send"));
        assertThat(request.headers().firstValue("Authorization"))
                .contains("Bearer access-123");

        JsonNode json = objectMapper.readTree(corpo(request));
        String messaggio = new String(
                Base64.getUrlDecoder().decode(json.path("raw").asText()),
                StandardCharsets.UTF_8);
        assertThat(messaggio)
                .contains("From: sistema@example.com")
                .contains("To: magazzino@example.com")
                .contains("Content-Type: text/plain; charset=UTF-8");

        String corpoBase64 = messaggio.substring(
                messaggio.indexOf("\r\n\r\n") + 4);
        String corpoDecodificato = new String(
                Base64.getMimeDecoder().decode(corpoBase64),
                StandardCharsets.UTF_8);
        assertThat(corpoDecodificato)
                .isEqualTo("Quantità attuale: 8");
    }

    private String corpo(HttpRequest request) {
        var subscriber = new BodySubscriber();
        request.bodyPublisher().orElseThrow().subscribe(subscriber);
        return subscriber.body();
    }

    private GmailOAuthProperties properties() {
        return new GmailOAuthProperties(
                "client-id",
                "client-secret",
                "refresh-token",
                URI.create("http://localhost:8081/login/oauth2/code/google"),
                URI.create("https://accounts.google.com/o/oauth2/v2/auth"),
                URI.create("https://oauth2.googleapis.com/token"),
                URI.create("https://gmail.googleapis.com/gmail/v1/"),
                false);
    }

    @SuppressWarnings("unchecked")
    private HttpResponse<String> response(int status) {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(status);
        return response;
    }

    private static class BodySubscriber
            implements Flow.Subscriber<ByteBuffer> {
        private final StringBuilder body = new StringBuilder();

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(ByteBuffer item) {
            byte[] bytes = new byte[item.remaining()];
            item.get(bytes);
            body.append(new String(bytes, StandardCharsets.UTF_8));
        }

        @Override
        public void onError(Throwable throwable) {
            throw new AssertionError(throwable);
        }

        @Override
        public void onComplete() {
        }

        String body() {
            return body.toString();
        }
    }
}
