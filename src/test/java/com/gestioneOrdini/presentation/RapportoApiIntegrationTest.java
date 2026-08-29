package com.gestioneOrdini.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import com.gestioneOrdini.domain.agente.repository.AgenteRepository;
import com.gestioneOrdini.domain.ordine.model.OrdineVendita;
import com.gestioneOrdini.domain.ordine.model.RigaOrdineVendita;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import com.gestioneOrdini.domain.ordine.repository.RigaOrdineVenditaRepository;
import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import com.gestioneOrdini.domain.prodotto.repository.ProdottoRepository;
import com.gestioneOrdini.domain.reporting.model.Rapporto;
import com.gestioneOrdini.domain.reporting.repository.RapportoRepository;
import com.gestioneOrdini.infrastructure.config.AbstractSqlServerIntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RapportoApiIntegrationTest extends AbstractSqlServerIntegrationTest {

    @Autowired private DataSource dataSource;
    @Autowired private TestRestTemplate restTemplate;
    @Autowired private RapportoRepository rapportoRepository;
    @Autowired private AgenteRepository agenteRepository;
    @Autowired private OrdineVenditaRepository ordineRepository;
    @Autowired private RigaOrdineVenditaRepository rigaOrdineRepository;
    @Autowired private ProdottoRepository prodottoRepository;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private ObjectMapper objectMapper;

    @BeforeAll
    void installaOggettiSqlDiReportistica() {
        applica("db/reporting/001_ordini_vendita_per_periodo.sql");
        applica("db/reporting/002_motore_rapporti_dinamici.sql");
        applica("db/reporting/003_ranking_venditori_per_periodo.sql");
        applica("db/reporting/004_vendite_ultimi_dodici_mesi.sql");
    }

    @Test
    void dovrebbeEsporreCatalogoParametriEdEseguireIlRapportoViaHttp() throws Exception {
        OrdineVendita ordine = salvaOrdine();
        aggiungiRiga(ordine);
        Rapporto rapporto = rapportoRepository
                .findByCodice("ORDINI_VENDITA_PER_PERIODO")
                .orElseThrow();
        LocalDate oggi = LocalDate.now();

        var catalogo = restTemplate.getForEntity("/api/rapporti", String.class);
        var parametri = restTemplate.getForEntity(
                "/api/rapporti/{id}/parametri", String.class, rapporto.getId());
        var esecuzione = restTemplate.postForEntity(
                "/api/rapporti/{id}/esegui",
                Map.of("parametri", Map.of(
                        "DataInizio", oggi.toString(),
                        "DataFine", oggi.toString(),
                        "ClienteId", ordine.getCliente().getId())),
                String.class,
                rapporto.getId());

        assertThat(catalogo.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(catalogo.getBody()).contains(
                "ORDINI_VENDITA_PER_PERIODO",
                "RANKING_VENDITORI_PER_PERIODO",
                "VENDITE_ULTIMI_DODICI_MESI");
        assertThat(parametri.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(parametri.getBody()).contains("DataInizio", "SELEZIONE", "haOpzioni");
        var opzioni = restTemplate.getForEntity(
                "/api/rapporti/{id}/parametri/ClienteId/opzioni",
                String.class,
                rapporto.getId());
        assertThat(opzioni.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(opzioni.getBody()).contains("valore", "etichetta");
        assertThat(esecuzione.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(esecuzione.getBody())
                .contains(ordine.getNumeroOrdine(), "numeroOrdine", "valoreTotale",
                        "totalizzare", "totali");
        var json = objectMapper.readTree(esecuzione.getBody());
        var colonne = new ArrayList<com.fasterxml.jackson.databind.JsonNode>();
        json.path("colonne").forEach(colonne::add);
        assertThat(colonne).anySatisfy(colonna -> {
            assertThat(colonna.path("nome").asText()).isEqualTo("valoreTotale");
            assertThat(colonna.path("totalizzare").asBoolean()).isTrue();
        });
        assertThat(json.at("/totali/valoreTotale").decimalValue())
                .isEqualByComparingTo("25.00");
    }

    @Test
    void dovrebbeRifiutareParametroNonRegistratoViaHttp() {
        Rapporto rapporto = rapportoRepository
                .findByCodice("ORDINI_VENDITA_PER_PERIODO")
                .orElseThrow();
        LocalDate oggi = LocalDate.now();

        var risposta = restTemplate.postForEntity(
                "/api/rapporti/{id}/esegui",
                Map.of("parametri", Map.of(
                        "DataInizio", oggi.toString(),
                        "DataFine", oggi.toString(),
                        "ComandoSql", "SELECT * FROM utenti")),
                String.class,
                rapporto.getId());

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(risposta.getBody()).contains("Parametro non riconosciuto");
    }

    @Test
    void dovrebbePubblicareUnNuovoRapportoCreatoSoltantoNelDatabase() {
        jdbcTemplate.execute("""
                CREATE OR ALTER PROCEDURE reporting.usp_report_demo_dinamico
                    @Messaggio NVARCHAR(50)
                AS
                BEGIN
                    SET NOCOUNT ON;
                    SELECT
                        @Messaggio AS messaggio,
                        N'Non deve essere esposto' AS colonnaNonRegistrata;
                END
                """);
        jdbcTemplate.execute("""
                EXEC reporting.usp_registra_rapporto
                    @Codice = N'DEMO_DINAMICO',
                    @Titolo = N'Demo dinamico',
                    @Descrizione = N'Creato soltanto nel database',
                    @NomeProcedura = N'reporting.usp_report_demo_dinamico',
                    @Attivo = 1,
                    @Ordine = 99
                """);
        jdbcTemplate.execute("""
                EXEC reporting.usp_sincronizza_parametri
                    @CodiceRapporto = N'DEMO_DINAMICO'
                """);
        jdbcTemplate.execute("""
                EXEC reporting.usp_configura_parametro
                    @CodiceRapporto = N'DEMO_DINAMICO',
                    @NomeParametro = N'Messaggio',
                    @Etichetta = N'Messaggio',
                    @TipoCampo = N'TESTO',
                    @Obbligatorio = 1,
                    @Ordine = 1
                """);
        jdbcTemplate.execute("""
                EXEC reporting.usp_configura_colonna
                    @CodiceRapporto = N'DEMO_DINAMICO',
                    @NomeColonna = N'messaggio',
                    @Etichetta = N'Messaggio',
                    @Formato = NULL,
                    @Ordine = 1,
                    @Visibile = 1,
                    @Totalizzare = 0
                """);

        Rapporto rapporto = rapportoRepository.findByCodice("DEMO_DINAMICO").orElseThrow();
        var risposta = restTemplate.postForEntity(
                "/api/rapporti/{id}/esegui",
                Map.of("parametri", Map.of("Messaggio", "Creato dal DBA")),
                String.class,
                rapporto.getId());

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(risposta.getBody()).contains("Creato dal DBA", "messaggio");
        assertThat(risposta.getBody()).doesNotContain(
                "colonnaNonRegistrata", "Non deve essere esposto");
    }

    private OrdineVendita salvaOrdine() {
        String suffisso = UUID.randomUUID().toString();
        Agente cliente = salvaAgente("Cliente " + suffisso, TipoAgente.CLIENTE, "c");
        Agente venditore = salvaAgente("Venditore " + suffisso, TipoAgente.VENDITORE, "v");
        Agente trasportatore = salvaAgente(
                "Trasportatore " + suffisso, TipoAgente.TRASPORTATORE, "t");
        return ordineRepository.save(new OrdineVendita(cliente, venditore, trasportatore));
    }

    private Agente salvaAgente(String nome, TipoAgente tipo, String prefisso) {
        String email = prefisso + UUID.randomUUID() + "@report.example.com";
        return agenteRepository.save(new Agente(nome, email, tipo, false));
    }

    private void aggiungiRiga(OrdineVendita ordine) {
        String codice = "R" + UUID.randomUUID().toString().replace("-", "").substring(0, 5);
        Prodotto prodotto = prodottoRepository.save(new Prodotto(
                codice,
                "Prodotto rapporto " + codice,
                new BigDecimal("5.00"),
                new BigDecimal("12.50"),
                100,
                10,
                false));
        rigaOrdineRepository.save(new RigaOrdineVendita(
                ordine.getId(), prodotto, 2, new BigDecimal("12.50")));
    }

    private void applica(String risorsa) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                new ClassPathResource(risorsa));
        populator.setSeparator("GO");
        DatabasePopulatorUtils.execute(populator, dataSource);
    }
}
