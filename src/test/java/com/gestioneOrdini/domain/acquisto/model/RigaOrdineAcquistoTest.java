package com.gestioneOrdini.domain.acquisto.model;

import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class RigaOrdineAcquistoTest {

    @Test
    void dovrebbeAggiornareProdottoQuantitaValoreETotale() {
        RigaOrdineAcquisto riga = new RigaOrdineAcquisto(
                30L,
                10L,
                prodotto(20L, "P001"),
                2,
                new BigDecimal("2.50"));

        riga.aggiorna(
                prodotto(21L, "P002"),
                4,
                new BigDecimal("3.75"));

        assertThat(riga.getProdotto().getCodice()).isEqualTo("P002");
        assertThat(riga.getQuantita()).isEqualTo(4);
        assertThat(riga.getValoreUnitario()).isEqualByComparingTo("3.75");
        assertThat(riga.getTotaleRiga()).isEqualByComparingTo("15.00");
    }

    private Prodotto prodotto(Long id, String codice) {
        return new Prodotto(
                id,
                codice,
                "Prodotto " + codice,
                new BigDecimal("2.50"),
                new BigDecimal("5.00"),
                10,
                2,
                false);
    }
}
