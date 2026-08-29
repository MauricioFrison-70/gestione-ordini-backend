package com.gestioneOrdini.domain.reporting.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RapportoTest {

    @Test
    void dovrebbeOrdinareIParametriPerOrdineDiVisualizzazione() {
        Rapporto rapporto = new Rapporto("TEST", "Test", "",
                "reporting.usp_test", true, List.of(
                new ParametroRapporto("Secondo", "Secondo", TipoParametroRapporto.TESTO, false, 2),
                new ParametroRapporto("Primo", "Primo", TipoParametroRapporto.DATA, true, 1)));

        assertThat(rapporto.getParametri()).extracting(ParametroRapporto::getNome)
                .containsExactly("Primo", "Secondo");
    }

    @Test
    void dovrebbeRifiutareUnaProceduraFuoriDalloSchemaReporting() {
        assertThatThrownBy(() -> new Rapporto("TEST", "Test", "",
                "dbo.usp_pericolosa", true, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("schema reporting");
    }

    @Test
    void dovrebbeRifiutareParametriConNomeDuplicato() {
        assertThatThrownBy(() -> new Rapporto("TEST", "Test", "",
                "reporting.usp_test", true, List.of(
                new ParametroRapporto("Data", "Data 1", TipoParametroRapporto.DATA, true, 1),
                new ParametroRapporto("data", "Data 2", TipoParametroRapporto.DATA, true, 2))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("univoci");
    }

    @Test
    void dovrebbeConsentireProceduraOpzioniSoloPerUnaSelezione() {
        assertThatThrownBy(() -> new ParametroRapporto(
                1L, "Nome", "Nome", "nvarchar", TipoParametroRapporto.TESTO,
                false, 1, null, "reporting.usp_opzioni"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("solo per un campo di selezione");
    }
}
