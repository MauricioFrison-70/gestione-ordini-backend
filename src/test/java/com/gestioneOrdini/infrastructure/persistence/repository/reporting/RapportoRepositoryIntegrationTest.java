package com.gestioneOrdini.infrastructure.persistence.repository.reporting;

import com.gestioneOrdini.domain.reporting.model.Rapporto;
import com.gestioneOrdini.domain.reporting.model.TipoParametroRapporto;
import com.gestioneOrdini.domain.reporting.repository.RapportoRepository;
import com.gestioneOrdini.infrastructure.config.AbstractSqlServerIntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RapportoRepositoryIntegrationTest extends AbstractSqlServerIntegrationTest {

    @Autowired private DataSource dataSource;
    @Autowired private RapportoRepository repository;

    @BeforeAll
    void installaMotoreDinamico() {
        applica("db/reporting/001_ordini_vendita_per_periodo.sql");
        applica("db/reporting/002_motore_rapporti_dinamici.sql");
    }

    @Test
    void dovrebbeLeggereRapportoEParametriConfiguratiSoltantoNelDatabase() {
        Rapporto rapporto = repository
                .findByCodice("ORDINI_VENDITA_PER_PERIODO")
                .orElseThrow();

        assertThat(rapporto.getTitolo()).isEqualTo("Ordini di vendita per periodo");
        assertThat(rapporto.getParametri()).extracting("nome")
                .containsExactly("DataInizio", "DataFine", "ClienteId", "Stato");
        assertThat(rapporto.getParametri().get(2).getTipo())
                .isEqualTo(TipoParametroRapporto.SELEZIONE);
        assertThat(rapporto.getParametri().get(2).getProceduraOpzioni())
                .isEqualTo("reporting.usp_opzioni_clienti");
        assertThat(rapporto.getParametri().get(3).getTipo())
                .isEqualTo(TipoParametroRapporto.SELEZIONE);
        assertThat(rapporto.getParametri().get(3).getProceduraOpzioni())
                .isEqualTo("reporting.usp_opzioni_stati_ordini_vendita");
        assertThat(rapporto.getColonne()).isNotEmpty();
        assertThat(rapporto.getColonne())
                .filteredOn(colonna -> colonna.getNome().equals("valoreTotale"))
                .singleElement()
                .satisfies(colonna -> assertThat(colonna.isTotalizzare()).isTrue());
        assertThat(repository.findAllAttivi()).extracting(Rapporto::getCodice)
                .contains("ORDINI_VENDITA_PER_PERIODO");
    }

    private void applica(String risorsa) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                new ClassPathResource(risorsa));
        populator.setSeparator("GO");
        DatabasePopulatorUtils.execute(populator, dataSource);
    }
}
