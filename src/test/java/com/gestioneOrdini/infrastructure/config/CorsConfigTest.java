package com.gestioneOrdini.infrastructure.config;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTest {

    @ParameterizedTest
    @ValueSource(strings = {"http://localhost:5173", "http://127.0.0.1:5173"})
    void dovrebbeConsentireLeOriginiDelFrontend(String origine) throws Exception {
        MockHttpServletRequest richiesta = new MockHttpServletRequest(
                "OPTIONS", "/api/ordini-vendita/10/righe");
        richiesta.setScheme("http");
        richiesta.setServerName("localhost");
        richiesta.setServerPort(8081);
        richiesta.addHeader(HttpHeaders.ORIGIN, origine);
        richiesta.addHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "DELETE");
        MockHttpServletResponse risposta = new MockHttpServletResponse();

        new CorsConfig().corsFilter().doFilter(
                richiesta, risposta, new MockFilterChain());

        assertThat(risposta.getStatus()).isEqualTo(200);
        assertThat(risposta.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
                .isEqualTo(origine);
    }
}
