package com.gestioneOrdini.presentation;

import com.gestioneOrdini.application.ordine.dto.RigaOrdineVenditaRequest;
import com.gestioneOrdini.application.ordine.dto.RigaOrdineVenditaResponse;
import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import com.gestioneOrdini.domain.agente.repository.AgenteRepository;
import com.gestioneOrdini.domain.ordine.model.OrdineVendita;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import com.gestioneOrdini.domain.ordine.repository.RigaOrdineVenditaRepository;
import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import com.gestioneOrdini.domain.prodotto.repository.ProdottoRepository;
import com.gestioneOrdini.infrastructure.config.AbstractSqlServerIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RigaOrdineVenditaApiIntegrationTest extends AbstractSqlServerIntegrationTest {
    private static final String ORIGINE_FRONTEND = "http://localhost:5173";
    private static final AtomicInteger SEQUENZA_CODICE = new AtomicInteger();

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private OrdineVenditaRepository ordineRepository;
    @Autowired private RigaOrdineVenditaRepository rigaRepository;
    @Autowired private AgenteRepository agenteRepository;
    @Autowired private ProdottoRepository prodottoRepository;

    @BeforeEach
    void configuraClientHttp() {
        restTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
    }

    @Test
    void dovrebbeGestireCrudCompletoViaHttpConCors() {
        OrdineVendita ordine = salvaOrdine(null);
        Prodotto prodotto = salvaProdotto();
        String url = url(ordine.getId());
        RigaOrdineVenditaRequest creazione = new RigaOrdineVenditaRequest(
                prodotto.getCodice(), 2, new BigDecimal("10.20"));

        var creata = restTemplate.exchange(
                url, HttpMethod.POST, richiesta(creazione), RigaOrdineVenditaResponse.class);

        assertThat(creata.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(creata.getHeaders().getAccessControlAllowOrigin()).isEqualTo(ORIGINE_FRONTEND);
        assertThat(creata.getBody()).isNotNull();
        assertThat(creata.getBody().totaleRiga()).isEqualByComparingTo("20.40");
        Long rigaId = creata.getBody().id();

        var elenco = restTemplate.exchange(
                url, HttpMethod.GET, richiesta(null), RigaOrdineVenditaResponse[].class);
        assertThat(elenco.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(elenco.getBody()).hasSize(1);
        assertThat(elenco.getBody()[0].codiceProdotto()).isEqualTo(prodotto.getCodice());

        RigaOrdineVenditaRequest modifica = new RigaOrdineVenditaRequest(
                prodotto.getCodice(), 3, new BigDecimal("11.00"));
        var aggiornata = restTemplate.exchange(
                url + "/" + rigaId, HttpMethod.PUT,
                richiesta(modifica), RigaOrdineVenditaResponse.class);
        assertThat(aggiornata.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(aggiornata.getBody()).isNotNull();
        assertThat(aggiornata.getBody().totaleRiga()).isEqualByComparingTo("33.00");

        var eliminata = restTemplate.exchange(
                url + "/" + rigaId, HttpMethod.DELETE,
                richiesta(null), Void.class);
        assertThat(eliminata.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(rigaRepository.findByIdAndOrdineVenditaId(
                rigaId, ordine.getId())).isEmpty();
    }

    @Test
    void dovrebbeRifiutareProdottoDuplicatoViaHttp() {
        OrdineVendita ordine = salvaOrdine(null);
        Prodotto prodotto = salvaProdotto();
        var request = new RigaOrdineVenditaRequest(
                prodotto.getCodice(), 1, new BigDecimal("5.00"));

        restTemplate.exchange(url(ordine.getId()), HttpMethod.POST,
                richiesta(request), RigaOrdineVenditaResponse.class);
        var duplicata = restTemplate.exchange(url(ordine.getId()), HttpMethod.POST,
                richiesta(request), String.class);

        assertThat(duplicata.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(duplicata.getBody()).contains("PRODOTTO_GIA_PRESENTE_NELL_ORDINE");
    }

    @Test
    void dovrebbeRifiutareModificaRigheDiOrdineRilasciatoViaHttp() {
        OrdineVendita ordine = salvaOrdine(LocalDate.of(2026, 8, 22));
        Prodotto prodotto = salvaProdotto();
        var request = new RigaOrdineVenditaRequest(
                prodotto.getCodice(), 1, new BigDecimal("5.00"));

        var risposta = restTemplate.exchange(url(ordine.getId()), HttpMethod.POST,
                richiesta(request), String.class);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(risposta.getHeaders().getAccessControlAllowOrigin()).isEqualTo(ORIGINE_FRONTEND);
        assertThat(risposta.getBody()).contains("ORDINE_VENDITA_NON_MODIFICABILE");
    }

    private String url(Long ordineId) {
        return "/api/ordini-vendita/" + ordineId + "/righe";
    }

    private <T> HttpEntity<T> richiesta(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setOrigin(ORIGINE_FRONTEND);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private OrdineVendita salvaOrdine(LocalDate dataRilascio) {
        String suffisso = UUID.randomUUID().toString().substring(0, 8);
        Agente cliente = salvaAgente("Cliente " + suffisso, TipoAgente.CLIENTE);
        Agente venditore = salvaAgente("Venditore " + suffisso, TipoAgente.VENDITORE);
        Agente trasportatore = salvaAgente(
                "Trasportatore " + suffisso, TipoAgente.TRASPORTATORE);
        return ordineRepository.save(new OrdineVendita(
                cliente, venditore, trasportatore, dataRilascio));
    }

    private Agente salvaAgente(String nome, TipoAgente tipo) {
        String email = UUID.randomUUID().toString() + "@righe-api.example.com";
        return agenteRepository.save(new Agente(nome, email, tipo, false));
    }

    private Prodotto salvaProdotto() {
        String codice = "T%05d".formatted(SEQUENZA_CODICE.incrementAndGet());
        return prodottoRepository.save(new Prodotto(
                codice, "Prodotto API",
                new BigDecimal("3.00"), new BigDecimal("5.00"),
                100, 10, false));
    }
}
