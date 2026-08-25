package com.gestioneOrdini.presentation;

import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import com.gestioneOrdini.domain.agente.repository.AgenteRepository;
import com.gestioneOrdini.domain.ordine.model.OrdineVendita;
import com.gestioneOrdini.domain.ordine.model.RigaOrdineVendita;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import com.gestioneOrdini.domain.ordine.repository.RigaOrdineVenditaRepository;
import com.gestioneOrdini.domain.prodotto.event.ScortaSottoMinimoEvent;
import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import com.gestioneOrdini.domain.prodotto.repository.ProdottoRepository;
import com.gestioneOrdini.application.prodotto.port.NotificaScortaService;
import com.gestioneOrdini.infrastructure.config.AbstractSqlServerIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrdineVenditaApiIntegrationTest extends AbstractSqlServerIntegrationTest {

    private static final String ORIGINE_FRONTEND = "http://localhost:5173";

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private OrdineVenditaRepository ordineRepository;
    @Autowired private AgenteRepository agenteRepository;
    @Autowired private RigaOrdineVenditaRepository rigaRepository;
    @Autowired private ProdottoRepository prodottoRepository;
    @MockBean private NotificaScortaService notificaScortaService;

    @BeforeEach
    void configuraClientHttp() {
        restTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
    }

    @Test
    void dovrebbeEliminareViaHttpUnOrdineNonRilasciatoENonAnnullatoEApplicareCors() {
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

    @Test
    void dovrebbeRifiutareViaHttpUnOrdineAnnullatoEApplicareCors() {
        OrdineVendita ordine = salvaOrdine(null, LocalDate.of(2026, 8, 22));

        var risposta = restTemplate.exchange(
                "/api/ordini-vendita/{id}",
                HttpMethod.DELETE,
                richiestaDalFrontend(),
                String.class,
                ordine.getId()
        );

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(risposta.getHeaders().getAccessControlAllowOrigin()).isEqualTo(ORIGINE_FRONTEND);
        assertThat(risposta.getBody()).contains("ORDINE_VENDITA_ANNULLATO");
        assertThat(ordineRepository.findById(ordine.getId())).isPresent();
    }

    @Test
    void dovrebbeRilasciareOrdineScalareLaGiacenzaENotificareScortaMinima() {
        OrdineVendita ordine = salvaOrdine(null);
        Prodotto prodotto = salvaProdotto("V" + UUID.randomUUID().toString().substring(0, 5), 6, 5);
        rigaRepository.save(new RigaOrdineVendita(
                ordine.getId(), prodotto, 2, new BigDecimal("10.00")));

        var risposta = restTemplate.postForEntity(
                "/api/ordini-vendita/{id}/rilasciare",
                null,
                String.class,
                ordine.getId());

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(prodottoRepository.findById(prodotto.getId()).orElseThrow().getQuantita())
                .isEqualTo(4);
        assertThat(ordineRepository.findById(ordine.getId()).orElseThrow().getDataRilascio())
                .isNotNull();
        verify(notificaScortaService, timeout(2_000))
                .inviaScortaSottoMinimo(any(ScortaSottoMinimoEvent.class));
    }

    @Test
    void dovrebbeMantenereTutteLeGiacenzeQuandoUnProdottoNonHaSaldo() {
        OrdineVendita ordine = salvaOrdine(null);
        Prodotto sufficiente = salvaProdotto(
                "S" + UUID.randomUUID().toString().substring(0, 5), 10, 2);
        Prodotto insufficiente = salvaProdotto(
                "I" + UUID.randomUUID().toString().substring(0, 5), 1, 1);
        rigaRepository.save(new RigaOrdineVendita(
                ordine.getId(), sufficiente, 2, new BigDecimal("10.00")));
        rigaRepository.save(new RigaOrdineVendita(
                ordine.getId(), insufficiente, 3, new BigDecimal("10.00")));

        var risposta = restTemplate.postForEntity(
                "/api/ordini-vendita/{id}/rilasciare",
                null,
                String.class,
                ordine.getId());

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(risposta.getBody()).contains("SCORTA_INSUFFICIENTE");
        assertThat(prodottoRepository.findById(sufficiente.getId()).orElseThrow().getQuantita())
                .isEqualTo(10);
        assertThat(prodottoRepository.findById(insufficiente.getId()).orElseThrow().getQuantita())
                .isEqualTo(1);
        assertThat(ordineRepository.findById(ordine.getId()).orElseThrow().getDataRilascio())
                .isNull();
    }

    private HttpEntity<Void> richiestaDalFrontend() {
        HttpHeaders headers = new HttpHeaders();
        headers.setOrigin(ORIGINE_FRONTEND);
        return new HttpEntity<>(headers);
    }

    private OrdineVendita salvaOrdine(LocalDate dataRilascio) {
        return salvaOrdine(dataRilascio, null);
    }

    private OrdineVendita salvaOrdine(LocalDate dataRilascio, LocalDate dataAnnullamento) {
        String suffisso = UUID.randomUUID().toString();
        Agente cliente = salvaAgente("Cliente " + suffisso, TipoAgente.CLIENTE, suffisso + ".c@example.com");
        Agente venditore = salvaAgente("Venditore " + suffisso, TipoAgente.VENDITORE, suffisso + ".v@example.com");
        Agente trasportatore = salvaAgente(
                "Trasportatore " + suffisso,
                TipoAgente.TRASPORTATORE,
                suffisso + ".t@example.com"
        );
        OrdineVendita ordine = new OrdineVendita(
                cliente, venditore, trasportatore, dataRilascio);
        if (dataAnnullamento != null) {
            ordine.annulla(dataAnnullamento);
        }
        return ordineRepository.save(ordine);
    }

    private Agente salvaAgente(String nome, TipoAgente tipo, String email) {
        return agenteRepository.save(new Agente(nome, email, tipo, false));
    }

    private Prodotto salvaProdotto(String codice, int quantita, int scortaMinima) {
        return prodottoRepository.save(new Prodotto(
                codice,
                "Prodotto vendita",
                new BigDecimal("5.00"),
                new BigDecimal("10.00"),
                quantita,
                scortaMinima,
                false));
    }
}
