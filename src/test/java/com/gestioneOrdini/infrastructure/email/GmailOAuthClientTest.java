package com.gestioneOrdini.infrastructure.email;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GmailOAuthClientTest {
    @Mock private GmailHttpTransport transport;

    private GmailOAuthClient client;

    @BeforeEach
    void setUp() {
        client = new GmailOAuthClient(
                transport, new ObjectMapper(), properties());
    }

    @Test
    void dovrebbeOttenereEriutilizzareLAccessToken() {
        HttpResponse<String> response = response(
                200,
                "{\"access_token\":\"access-123\",\"expires_in\":3600}");
        when(transport.invia(org.mockito.ArgumentMatchers
                .any(HttpRequest.class))).thenReturn(response);

        assertThat(client.ottieniAccessToken()).isEqualTo("access-123");
        assertThat(client.ottieniAccessToken()).isEqualTo("access-123");

        ArgumentCaptor<HttpRequest> captor =
                ArgumentCaptor.forClass(HttpRequest.class);
        verify(transport, times(1)).invia(captor.capture());
        assertThat(captor.getValue().uri())
                .isEqualTo(URI.create("https://oauth2.googleapis.com/token"));
        assertThat(captor.getValue().method()).isEqualTo("POST");
        assertThat(captor.getValue().headers()
                .firstValue("Content-Type"))
                .contains("application/x-www-form-urlencoded");
    }

    @Test
    void dovrebbeCreareLaUrlDiAutorizzazioneConAccessoOffline() {
        URI uri = client.creaUrlAutorizzazione("stato-123");

        assertThat(uri.toString())
                .startsWith("https://accounts.google.com/o/oauth2/v2/auth?")
                .contains("access_type=offline")
                .contains("prompt=consent")
                .contains("state=stato-123")
                .contains("scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fgmail.send")
                .contains("redirect_uri=http%3A%2F%2Flocalhost%3A8081%2Flogin%2Foauth2%2Fcode%2Fgoogle");
    }

    @Test
    void dovrebbeScambiareIlCodiceConIlRefreshToken() {
        HttpResponse<String> response = response(
                200,
                "{\"access_token\":\"access\",\"refresh_token\":\"refresh-123\"}");
        when(transport.invia(org.mockito.ArgumentMatchers
                .any(HttpRequest.class))).thenReturn(response);

        assertThat(client.scambiaCodicePerRefreshToken("code-123"))
                .isEqualTo("refresh-123");
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
    private HttpResponse<String> response(int status, String body) {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(status);
        when(response.body()).thenReturn(body);
        return response;
    }
}
