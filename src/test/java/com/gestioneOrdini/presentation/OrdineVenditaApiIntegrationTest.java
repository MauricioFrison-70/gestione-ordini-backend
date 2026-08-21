package com.gestioneOrdini.presentation;

import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import com.gestioneOrdini.domain.agente.repository.AgenteRepository;
import com.gestioneOrdini.domain.ordine.model.OrdineVendita;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import com.gestioneOrdini.infrastructure.config.AbstractSqlServerIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrdineVenditaApiIntegrationTest extends AbstractSqlServerIntegrationTest {

    private static final String ORIGINE_FRONTEND = "http://localhost:5173";

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private OrdineVenditaRepository ordineRepository;
    @Autowired private AgenteRepository agenteRepository;

    @Test
    void dovrebbeEliminareViaHttpUnOrdineNonRilasciatoEApplicareCors() {
        OrdineVendita ordine = salvaOrdine(null);

        var risposta = restTemplate.exchange(
                "/api/ordini-vendita/{id}",
                HttpMethod.DELETE,
                richiestaDalFrontend(),
                Void.class,
                ordine.getId()
        );

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(risposta.getHeaders().getAccessControlAllowOrigin()).isEqualTo(ORIGINE_FRONTEND);
        assertThat(ordineRepository.findById(ordine.getId())).isEmpty();
    }

    @Test
    void dovrebbeRifiutareViaHttpUnOrdineRilasciatoEApplicareCors() {
        OrdineVendita ordine = salvaOrdine(LocalDate.of(2026, 8, 21));

        var risposta = restTemplate.exchange(
                "/api/ordini-vendita/{id}",
                HttpMethod.DELETE,
                richiestaDalFrontend(),
                String.class,
                ordine.getId()
        );

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(risposta.getHeaders().getAccessControlAllowOrigin()).isEqualTo(ORIGINE_FRONTEND);
        assertThat(risposta.getBody()).contains("ORDINE_VENDITA_RILASCIATO");
        assertThat(ordineRepository.findById(ordine.getId())).isPresent();
    }

    private HttpEntity<Void> richiestaDalFrontend() {
        HttpHeaders headers = new HttpHeaders();
        headers.setOrigin(ORIGINE_FRONTEND);
        return new HttpEntity<>(headers);
    }

    private OrdineVendita salvaOrdine(LocalDate dataRilascio) {
        String suffisso = UUID.randomUUID().toString();
        Agente cliente = salvaAgente("Cliente " + suffisso, TipoAgente.CLIENTE, suffisso + ".c@example.com");
        Agente venditore = salvaAgente("Venditore " + suffisso, TipoAgente.VENDITORE, suffisso + ".v@example.com");
        Agente trasportatore = salvaAgente(
                "Trasportatore " + suffisso,
                TipoAgente.TRASPORTATORE,
                suffisso + ".t@example.com"
        );
        return ordineRepository.save(
                new OrdineVendita(cliente, venditore, trasportatore, dataRilascio)
        );
    }

    private Agente salvaAgente(String nome, TipoAgente tipo, String email) {
        return agenteRepository.save(new Agente(nome, email, tipo, false));
    }
}
