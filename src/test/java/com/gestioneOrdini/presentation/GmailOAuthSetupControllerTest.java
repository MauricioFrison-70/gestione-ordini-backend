package com.gestioneOrdini.presentation;

import com.gestioneOrdini.infrastructure.email.GmailOAuthClient;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GmailOAuthSetupControllerTest {
    @Mock private GmailOAuthClient oauthClient;
    @Mock private HttpSession session;

    @Test
    void dovrebbeReindirizzareVersoGoogleConUnoStatoCasuale() {
        var controller = new GmailOAuthSetupController(oauthClient);
        ArgumentCaptor<String> state = ArgumentCaptor.forClass(String.class);
        when(oauthClient.creaUrlAutorizzazione(
                org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(URI.create("https://accounts.google.com/oauth"));

        var response = controller.autorizza(session);

        verify(session).setAttribute(
                org.mockito.ArgumentMatchers.eq(
                        GmailOAuthSetupController.SESSION_STATE),
                state.capture());
        assertThat(state.getValue()).isNotBlank();
        assertThat(response.getStatusCode().value()).isEqualTo(302);
        assertThat(response.getHeaders().getLocation())
                .isEqualTo(URI.create("https://accounts.google.com/oauth"));
    }

    @Test
    void dovrebbeMostrareIlRefreshTokenDopoIlCallbackValido() {
        var controller = new GmailOAuthSetupController(oauthClient);
        when(session.getAttribute(GmailOAuthSetupController.SESSION_STATE))
                .thenReturn("state-123");
        when(oauthClient.scambiaCodicePerRefreshToken("code-123"))
                .thenReturn("refresh-123");

        var response = controller.callback(
                "code-123", "state-123", null, session);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody())
                .contains("GMAIL_OAUTH_REFRESH_TOKEN=refresh-123");
        assertThat(response.getHeaders().getCacheControl())
                .contains("no-store");
        verify(session).removeAttribute(
                GmailOAuthSetupController.SESSION_STATE);
    }

    @Test
    void dovrebbeRifiutareUnCallbackConStatoNonValido() {
        var controller = new GmailOAuthSetupController(oauthClient);
        when(session.getAttribute(GmailOAuthSetupController.SESSION_STATE))
                .thenReturn("state-atteso");

        var response = controller.callback(
                "code-123", "state-diverso", null, session);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        verify(oauthClient, never())
                .scambiaCodicePerRefreshToken("code-123");
    }
}
