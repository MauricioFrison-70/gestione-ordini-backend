package com.gestioneOrdini.presentation;

import com.gestioneOrdini.application.prodotto.port.NotificaScortaService;
import com.gestioneOrdini.domain.acquisto.model.OrdineAcquisto;
import com.gestioneOrdini.domain.acquisto.model.RigaOrdineAcquisto;
import com.gestioneOrdini.domain.acquisto.repository.OrdineAcquistoRepository;
import com.gestioneOrdini.domain.acquisto.repository.RigaOrdineAcquistoRepository;
import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import com.gestioneOrdini.domain.agente.repository.AgenteRepository;
import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import com.gestioneOrdini.domain.prodotto.event.ScortaRipristinataEvent;
import com.gestioneOrdini.domain.prodotto.repository.ProdottoRepository;
import com.gestioneOrdini.infrastructure.config.AbstractSqlServerIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrdineAcquistoApiIntegrationTest extends AbstractSqlServerIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private OrdineAcquistoRepository ordineRepository;
    @Autowired private RigaOrdineAcquistoRepository rigaRepository;
    @Autowired private AgenteRepository agenteRepository;
    @Autowired private ProdottoRepository prodottoRepository;
    @MockBean private NotificaScortaService notificaScortaService;

    @Test
    void dovrebbeRicevereLaMerceEAggiornareLaGiacenzaViaHttp() {
        String suffisso = UUID.randomUUID().toString();
        Agente fornitore = agenteRepository.save(new Agente(
                "Fornitore " + suffisso,
                suffisso + "@example.com",
                TipoAgente.FORNITORE,
                false));
        Prodotto prodotto = prodottoRepository.save(new Prodotto(
                "A" + suffisso.substring(0, 5),
                "Prodotto acquisto",
                new BigDecimal("2.50"),
                new BigDecimal("4.00"),
                1,
                2,
                false));
        OrdineAcquisto ordine = ordineRepository.save(new OrdineAcquisto(fornitore));
        rigaRepository.save(new RigaOrdineAcquisto(
                ordine.getId(), prodotto, 5, new BigDecimal("2.50")));

        var risposta = restTemplate.postForEntity(
                "/api/ordini-acquisto/{id}/ricevere",
                null,
                String.class,
                ordine.getId());

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(prodottoRepository.findById(prodotto.getId()).orElseThrow().getQuantita())
                .isEqualTo(6);
        assertThat(ordineRepository.findById(ordine.getId()).orElseThrow().getDataRicevimento())
                .isNotNull();
        verify(notificaScortaService, timeout(2_000))
                .inviaScortaRipristinata(any(ScortaRipristinataEvent.class));
    }
}
