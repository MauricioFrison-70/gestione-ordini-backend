package com.gestioneOrdini.infrastructure.reporting;

import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import com.gestioneOrdini.domain.agente.repository.AgenteRepository;
import com.gestioneOrdini.domain.ordine.model.OrdineVendita;
import com.gestioneOrdini.domain.ordine.model.RigaOrdineVendita;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import com.gestioneOrdini.domain.ordine.repository.RigaOrdineVenditaRepository;
import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import com.gestioneOrdini.domain.prodotto.repository.ProdottoRepository;
import com.gestioneOrdini.infrastructure.config.AbstractSqlServerIntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("integration")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrdiniVenditaReportSqlIntegrationTest extends AbstractSqlServerIntegrationTest {

    private static final String PROCEDURA =
            "reporting.usp_report_ordini_vendita_per_periodo";
    private static final String PROCEDURA_RANKING =
            "reporting.usp_report_ranking_venditori_per_periodo";
    private static final String PROCEDURA_ULTIMI_DODICI_MESI =
            "reporting.usp_report_vendite_ultimi_dodici_mesi";

    @Autowired private DataSource dataSource;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private AgenteRepository agenteRepository;
    @Autowired private ProdottoRepository prodottoRepository;
    @Autowired private OrdineVenditaRepository ordineRepository;
    @Autowired private RigaOrdineVenditaRepository rigaRepository;

    @BeforeAll
    void installaOggettiSqlDiReportistica() {
        applica("db/reporting/001_ordini_vendita_per_periodo.sql");
        applica("db/reporting/002_motore_rapporti_dinamici.sql");
        applica("db/reporting/003_ranking_venditori_per_periodo.sql");
        applica("db/reporting/004_vendite_ultimi_dodici_mesi.sql");
    }

    @Test
    void dovrebbeCreareViewConTotaleEStatoDellOrdine() {
        OrdineVendita ordine = salvaOrdine("View", LocalDate.now(), null);
        aggiungiRiga(ordine, "RV001", 2, "10.20");
        aggiungiRiga(ordine, "RV002", 1, "5.10");

        Map<String, Object> riga = jdbcTemplate.queryForMap("""
                SELECT numeroOrdine, stato, valoreTotale
                FROM reporting.vw_ordini_vendita
                WHERE idOrdine = ?
                """, ordine.getId());

        assertThat(riga.get("numeroOrdine")).isEqualTo(ordine.getNumeroOrdine());
        assertThat(riga.get("stato")).isEqualTo("RILASCIATO");
        assertThat((BigDecimal) riga.get("valoreTotale"))
                .isEqualByComparingTo("25.50");
    }

    @Test
    void dovrebbeFiltrareLaProceduraPerPeriodoECliente() {
        LocalDate oggi = LocalDate.now();
        OrdineVendita ordineClienteAtteso = salvaOrdine(
                "ClienteAtteso", null, null);
        aggiungiRiga(ordineClienteAtteso, "RP001", 3, "7.00");

        OrdineVendita ordineAltroCliente = salvaOrdine(
                "AltroCliente", null, oggi);
        aggiungiRiga(ordineAltroCliente, "RP002", 1, "9.00");

        List<Map<String, Object>> risultato = eseguiProcedura(
                oggi, oggi, ordineClienteAtteso.getCliente().getId(), null);

        assertThat(risultato).hasSize(1);
        assertThat(risultato.get(0).get("numeroOrdine"))
                .isEqualTo(ordineClienteAtteso.getNumeroOrdine());
        assertThat(risultato.get(0).get("clienteId"))
                .isEqualTo(ordineClienteAtteso.getCliente().getId());
        assertThat(risultato.get(0).get("stato"))
                .isEqualTo("NON_RILASCIATO");
        assertThat((BigDecimal) risultato.get(0).get("valoreTotale"))
                .isEqualByComparingTo("21.00");
    }

    @Test
    void dovrebbeAccettareClienteOpzionale() {
        LocalDate oggi = LocalDate.now();
        salvaOrdine("ClienteUno", null, null);
        salvaOrdine("ClienteDue", null, oggi);

        List<Map<String, Object>> risultato = eseguiProcedura(
                oggi, oggi, null, null);

        assertThat(risultato).hasSize(2);
        assertThat(risultato)
                .extracting(riga -> riga.get("stato"))
                .containsExactlyInAnyOrder("NON_RILASCIATO", "ANNULLATO");
    }

    @Test
    void dovrebbeRifiutarePeriodoInvalido() {
        assertThatThrownBy(() -> eseguiProcedura(
                LocalDate.of(2026, 8, 31),
                LocalDate.of(2026, 8, 1),
                null,
                null))
                .hasMessageContaining(
                        "La data iniziale non può essere successiva alla data finale");
    }

    @Test
    void dovrebbeFiltrareLaProceduraPerStato() {
        LocalDate oggi = LocalDate.now();
        salvaOrdine("NonRilasciato", null, null);
        salvaOrdine("Rilasciato", oggi, null);
        salvaOrdine("Annullato", null, oggi);

        List<Map<String, Object>> risultato = eseguiProcedura(
                oggi, oggi, null, "RILASCIATO");

        assertThat(risultato).hasSize(1);
        assertThat(risultato.get(0).get("stato")).isEqualTo("RILASCIATO");
    }

    @Test
    void dovrebbeRifiutareStatoInvalido() {
        LocalDate oggi = LocalDate.now();

        assertThatThrownBy(() -> eseguiProcedura(
                oggi, oggi, null, "INESISTENTE"))
                .hasMessageContaining("Lo stato dell'ordine non è valido");
    }

    @Test
    void dovrebbeGenerareIlRankingDeiVenditoriDalMaggioreAlMinore() {
        LocalDate oggi = LocalDate.now();
        Agente primoVenditore = salvaAgente("Venditore Ranking Alfa", TipoAgente.VENDITORE);
        Agente secondoVenditore = salvaAgente("Venditore Ranking Beta", TipoAgente.VENDITORE);

        OrdineVendita primoOrdine = salvaOrdinePerVenditore(primoVenditore, "RankingA1");
        OrdineVendita secondoOrdine = salvaOrdinePerVenditore(primoVenditore, "RankingA2");
        OrdineVendita terzoOrdine = salvaOrdinePerVenditore(secondoVenditore, "RankingB1");
        OrdineVendita annullato = salvaOrdinePerVenditore(secondoVenditore, "RankingB2");
        primoOrdine.rilascia(oggi);
        secondoOrdine.rilascia(oggi);
        terzoOrdine.rilascia(oggi);
        ordineRepository.save(primoOrdine);
        ordineRepository.save(secondoOrdine);
        ordineRepository.save(terzoOrdine);
        annullato.annulla(oggi);
        ordineRepository.save(annullato);

        aggiungiRiga(primoOrdine, "RKA001", 2, "10.00");
        aggiungiRiga(secondoOrdine, "RKA002", 1, "15.00");
        aggiungiRiga(terzoOrdine, "RKB001", 3, "10.00");
        aggiungiRiga(annullato, "RKB002", 10, "10.00");

        List<Map<String, Object>> risultato = eseguiRanking(oggi, oggi);

        assertThat(risultato).hasSize(2);
        assertThat(((Number) risultato.get(0).get("posizione")).longValue()).isEqualTo(1L);
        assertThat(risultato.get(0).get("venditore")).isEqualTo(primoVenditore.getNome());
        assertThat(((Number) risultato.get(0).get("numeroOrdini")).longValue()).isEqualTo(2L);
        assertThat((BigDecimal) risultato.get(0).get("valoreTotale"))
                .isEqualByComparingTo("35.00");
        assertThat(((Number) risultato.get(1).get("posizione")).longValue()).isEqualTo(2L);
        assertThat(risultato.get(1).get("venditore")).isEqualTo(secondoVenditore.getNome());
        assertThat((BigDecimal) risultato.get(1).get("valoreTotale"))
                .isEqualByComparingTo("30.00");
    }

    @Test
    void dovrebbeTotalizzareGliUltimiDodiciMesiInclusiQuelliSenzaVendite() {
        LocalDate meseCorrente = LocalDate.now().withDayOfMonth(1);
        OrdineVendita ordineCorrente = salvaOrdine(
                "VenditeMeseCorrente", LocalDate.now(), null);
        OrdineVendita ordinePrecedente = salvaOrdine(
                "VenditeMesePrecedente", LocalDate.now(), null);
        OrdineVendita ordineFuoriPeriodo = salvaOrdine(
                "VenditeFuoriPeriodo", LocalDate.now(), null);
        OrdineVendita ordineAnnullato = salvaOrdine("VenditeAnnullate", null, LocalDate.now());

        impostaDataRegistrazione(ordinePrecedente, meseCorrente.minusMonths(1).plusDays(10));
        impostaDataRegistrazione(ordineFuoriPeriodo, meseCorrente.minusMonths(12).plusDays(10));

        aggiungiRiga(ordineCorrente, "UDM001", 2, "10.00");
        aggiungiRiga(ordinePrecedente, "UDM002", 3, "10.00");
        aggiungiRiga(ordineFuoriPeriodo, "UDM003", 10, "10.00");
        aggiungiRiga(ordineAnnullato, "UDM004", 20, "10.00");

        List<Map<String, Object>> risultato = eseguiUltimiDodiciMesi();

        assertThat(risultato).hasSize(12);
        assertThat(((Date) risultato.get(0).get("periodo")).toLocalDate())
                .isEqualTo(meseCorrente.minusMonths(11));
        assertThat(((Date) risultato.get(11).get("periodo")).toLocalDate())
                .isEqualTo(meseCorrente);

        Map<String, Object> mesePrecedente = risultato.get(10);
        assertThat(((Number) mesePrecedente.get("numeroOrdini")).longValue()).isEqualTo(1L);
        assertThat((BigDecimal) mesePrecedente.get("valoreTotale"))
                .isEqualByComparingTo("30.00");

        Map<String, Object> corrente = risultato.get(11);
        assertThat(((Number) corrente.get("numeroOrdini")).longValue()).isEqualTo(1L);
        assertThat((BigDecimal) corrente.get("valoreTotale"))
                .isEqualByComparingTo("20.00");
        assertThat(risultato.subList(0, 10))
                .allSatisfy(riga -> {
                    assertThat(((Number) riga.get("numeroOrdini")).longValue()).isZero();
                    assertThat((BigDecimal) riga.get("valoreTotale"))
                            .isEqualByComparingTo("0.00");
                });
    }

    @Test
    void dovrebbeConsentireSoloLEsecuzioneDellaProceduraAllaRole() {
        LocalDate oggi = LocalDate.now();
        salvaOrdine("Permessi", null, null);

        jdbcTemplate.execute("""
                CREATE USER report_executor_test WITHOUT LOGIN;
                ALTER ROLE report_executor_role ADD MEMBER report_executor_test;
                """);

        jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
            try (Statement context = connection.createStatement()) {
                context.execute("EXECUTE AS USER = 'report_executor_test'");
            }

            try {
                try (CallableStatement statement = connection.prepareCall(
                        "{call " + PROCEDURA + "(?, ?, ?, ?)}")) {
                    statement.setDate(1, Date.valueOf(oggi));
                    statement.setDate(2, Date.valueOf(oggi));
                    statement.setNull(3, Types.BIGINT);
                    statement.setNull(4, Types.NVARCHAR);

                    try (ResultSet resultSet = statement.executeQuery()) {
                        assertThat(resultSet.next()).isTrue();
                    }
                }

                assertThatThrownBy(() -> selezionaDirettamenteOrdini(connection))
                        .isInstanceOf(SQLException.class)
                        .hasMessageContaining("SELECT permission was denied");
            } finally {
                try (Statement context = connection.createStatement()) {
                    context.execute("REVERT");
                }
            }
            return null;
        });
    }

    private List<Map<String, Object>> eseguiProcedura(
            LocalDate dataInizio,
            LocalDate dataFine,
            Long clienteId,
            String stato) {

        return jdbcTemplate.query(
                connection -> {
                    CallableStatement statement = connection.prepareCall(
                            "{call " + PROCEDURA + "(?, ?, ?, ?)}");
                    statement.setDate(1, Date.valueOf(dataInizio));
                    statement.setDate(2, Date.valueOf(dataFine));
                    if (clienteId == null) {
                        statement.setNull(3, Types.BIGINT);
                    } else {
                        statement.setLong(3, clienteId);
                    }
                    if (stato == null) {
                        statement.setNull(4, Types.NVARCHAR);
                    } else {
                        statement.setString(4, stato);
                    }
                    statement.setQueryTimeout(30);
                    statement.setMaxRows(500);
                    return statement;
                },
                (resultSet, numeroRiga) -> {
                    Map<String, Object> riga = new java.util.LinkedHashMap<>();
                    int colonne = resultSet.getMetaData().getColumnCount();
                    for (int indice = 1; indice <= colonne; indice++) {
                        riga.put(resultSet.getMetaData().getColumnLabel(indice),
                                resultSet.getObject(indice));
                    }
                    return riga;
                });
    }

    private List<Map<String, Object>> eseguiRanking(
            LocalDate dataInizio,
            LocalDate dataFine) {
        return jdbcTemplate.query(
                connection -> {
                    CallableStatement statement = connection.prepareCall(
                            "{call " + PROCEDURA_RANKING + "(?, ?)}");
                    statement.setDate(1, Date.valueOf(dataInizio));
                    statement.setDate(2, Date.valueOf(dataFine));
                    return statement;
                },
                (resultSet, numeroRiga) -> {
                    Map<String, Object> riga = new java.util.LinkedHashMap<>();
                    int colonne = resultSet.getMetaData().getColumnCount();
                    for (int indice = 1; indice <= colonne; indice++) {
                        riga.put(resultSet.getMetaData().getColumnLabel(indice),
                                resultSet.getObject(indice));
                    }
                    return riga;
                });
    }

    private List<Map<String, Object>> eseguiUltimiDodiciMesi() {
        return jdbcTemplate.query(
                "EXEC " + PROCEDURA_ULTIMI_DODICI_MESI,
                (resultSet, numeroRiga) -> {
                    Map<String, Object> riga = new java.util.LinkedHashMap<>();
                    int colonne = resultSet.getMetaData().getColumnCount();
                    for (int indice = 1; indice <= colonne; indice++) {
                        riga.put(resultSet.getMetaData().getColumnLabel(indice),
                                resultSet.getObject(indice));
                    }
                    return riga;
                });
    }

    private void impostaDataRegistrazione(OrdineVendita ordine, LocalDate data) {
        jdbcTemplate.update(
                "UPDATE dbo.ordini_vendita SET created_at = ? WHERE id = ?",
                java.sql.Timestamp.valueOf(data.atTime(12, 0)),
                ordine.getId());
    }

    private OrdineVendita salvaOrdine(
            String suffisso,
            LocalDate dataRilascio,
            LocalDate dataAnnullamento) {

        Agente cliente = salvaAgente("Cliente " + suffisso, TipoAgente.CLIENTE);
        Agente venditore = salvaAgente(
                "Venditore " + suffisso, TipoAgente.VENDITORE);
        Agente trasportatore = salvaAgente(
                "Trasportatore " + suffisso, TipoAgente.TRASPORTATORE);

        OrdineVendita ordine = new OrdineVendita(
                cliente, venditore, trasportatore, dataRilascio);
        if (dataAnnullamento != null) {
            ordine.annulla(dataAnnullamento);
        }
        return ordineRepository.save(ordine);
    }

    private OrdineVendita salvaOrdinePerVenditore(Agente venditore, String suffisso) {
        Agente cliente = salvaAgente("Cliente " + suffisso, TipoAgente.CLIENTE);
        Agente trasportatore = salvaAgente(
                "Trasportatore " + suffisso, TipoAgente.TRASPORTATORE);
        return ordineRepository.save(new OrdineVendita(cliente, venditore, trasportatore));
    }

    private void applica(String risorsa) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                new ClassPathResource(risorsa));
        populator.setSqlScriptEncoding(StandardCharsets.UTF_8.name());
        populator.setSeparator("GO");
        DatabasePopulatorUtils.execute(populator, dataSource);
    }

    private Agente salvaAgente(String nome, TipoAgente tipo) {
        String email = nome.toLowerCase()
                .replace(" ", "")
                + "@report.example.com";
        return agenteRepository.save(new Agente(nome, email, tipo, false));
    }

    private void aggiungiRiga(
            OrdineVendita ordine,
            String codiceProdotto,
            int quantita,
            String valoreUnitario) {

        Prodotto prodotto = prodottoRepository.save(new Prodotto(
                codiceProdotto,
                "Prodotto " + codiceProdotto,
                new BigDecimal("5.00"),
                new BigDecimal("10.00"),
                100,
                10,
                false));

        rigaRepository.save(new RigaOrdineVendita(
                ordine.getId(),
                prodotto,
                quantita,
                new BigDecimal(valoreUnitario)));
    }

    private void selezionaDirettamenteOrdini(java.sql.Connection connection)
            throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet ignored = statement.executeQuery(
                     "SELECT TOP 1 * FROM dbo.ordini_vendita")) {
            ignored.next();
        }
    }
}
