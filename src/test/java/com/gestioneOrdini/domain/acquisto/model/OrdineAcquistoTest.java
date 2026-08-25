package com.gestioneOrdini.domain.acquisto.model;

import com.gestioneOrdini.domain.acquisto.exception.OrdineAcquistoNonModificabileException;
import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrdineAcquistoTest {

    @Test
    void dovrebbeAccettareSoltantoUnFornitore() {
        OrdineAcquisto ordine = new OrdineAcquisto(agente(TipoAgente.FORNITORE));
        assertThat(ordine.getFornitore().getTipoAgente())
                .isEqualTo(TipoAgente.FORNITORE);

        assertThatThrownBy(() -> new OrdineAcquisto(agente(TipoAgente.CLIENTE)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("FORNITORE");
    }

    @Test
    void dovrebbeImpedireRicevimentoDoppio() {
        OrdineAcquisto ordine = new OrdineAcquisto(agente(TipoAgente.FORNITORE));
        ordine.ricevi(LocalDate.of(2026, 8, 22));

        assertThatThrownBy(() -> ordine.ricevi(LocalDate.of(2026, 8, 23)))
                .isInstanceOf(OrdineAcquistoNonModificabileException.class)
                .hasMessageContaining("già stato ricevuto");
    }

    @Test
    void dovrebbeImpedireRicevimentoDopoAnnullamento() {
        OrdineAcquisto ordine = new OrdineAcquisto(agente(TipoAgente.FORNITORE));
        ordine.annulla(LocalDate.of(2026, 8, 22));

        assertThatThrownBy(() -> ordine.ricevi(LocalDate.of(2026, 8, 23)))
                .isInstanceOf(OrdineAcquistoNonModificabileException.class)
                .hasMessageContaining("già stato annullato");
    }

    private Agente agente(TipoAgente tipo) {
        return new Agente(1L, tipo.name(), "agente@example.com", tipo, false);
    }
}
