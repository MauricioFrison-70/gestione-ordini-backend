package com.gestioneOrdini.domain.ordine.model;

import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrdineVenditaTest {

    @Test
    void dovrebbeCreareOrdineConIRequiredTipiDiAgente() {
        OrdineVendita ordine = nuovoOrdine();

        assertThat(ordine.getCliente().getTipoAgente()).isEqualTo(TipoAgente.CLIENTE);
        assertThat(ordine.getVenditore().getTipoAgente()).isEqualTo(TipoAgente.VENDITORE);
        assertThat(ordine.getTrasportatore().getTipoAgente()).isEqualTo(TipoAgente.TRASPORTATORE);
        assertThat(ordine.getDataRilascio()).isNull();
        assertThat(ordine.getDataAnnullamento()).isNull();
    }

    @Test
    void dovrebbeRilasciareOrdinePendente() {
        OrdineVendita ordine = nuovoOrdine();
        ordine.rilascia(LocalDate.of(2026, 8, 21));
        assertThat(ordine.getDataRilascio()).isEqualTo(LocalDate.of(2026, 8, 21));
    }

    @Test
    void dovrebbeAnnullareOrdinePendente() {
        OrdineVendita ordine = nuovoOrdine();
        ordine.annulla(LocalDate.of(2026, 8, 22));
        assertThat(ordine.getDataAnnullamento()).isEqualTo(LocalDate.of(2026, 8, 22));
    }

    @Test
    void dovrebbeRifiutareAnnullamentoSeOrdineGiaRilasciato() {
        OrdineVendita ordine = nuovoOrdine();
        ordine.rilascia(LocalDate.of(2026, 8, 21));

        assertThatThrownBy(() -> ordine.annulla(LocalDate.of(2026, 8, 22)))
                .isInstanceOf(com.gestioneOrdini.domain.ordine.exception.OrdineVenditaNonModificabileException.class)
                .hasMessageContaining("già stato rilasciato");
    }

    @Test
    void dovrebbeRifiutareClienteDiTipoErrato() {
        assertThatThrownBy(() -> new OrdineVendita(
                agente(1L, TipoAgente.FORNITORE),
                agente(2L, TipoAgente.VENDITORE),
                agente(3L, TipoAgente.TRASPORTATORE)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CLIENTE");
    }

    private OrdineVendita nuovoOrdine() {
        return new OrdineVendita(agente(1L, TipoAgente.CLIENTE),
                agente(2L, TipoAgente.VENDITORE),
                agente(3L, TipoAgente.TRASPORTATORE));
    }

    private Agente agente(Long id, TipoAgente tipo) {
        return new Agente(id, tipo.name(), tipo.name().toLowerCase() + "@example.com", tipo, false);
    }
}
