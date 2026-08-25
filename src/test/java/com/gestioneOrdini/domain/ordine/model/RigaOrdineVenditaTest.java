package com.gestioneOrdini.domain.ordine.model;

import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RigaOrdineVenditaTest {

    @Test
    void dovrebbeCreareRigaECalcolareTotaleArrotondato() {
        RigaOrdineVendita riga = new RigaOrdineVendita(
                10L, prodotto(), 2, new BigDecimal("10.20"));

        assertThat(riga.getProdotto().getCodice()).isEqualTo("P001");
        assertThat(riga.getQuantita()).isEqualTo(2);
        assertThat(riga.getValoreUnitario()).isEqualByComparingTo("10.20");
        assertThat(riga.getTotaleRiga()).isEqualByComparingTo("20.40");
    }

    @Test
    void dovrebbeRifiutareQuantitaNonPositiva() {
        assertThatThrownBy(() -> new RigaOrdineVendita(
                10L, prodotto(), 0, new BigDecimal("10.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantità");
        assertThatThrownBy(() -> new RigaOrdineVendita(
                10L, prodotto(), 1_000_000_000, new BigDecimal("10.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("999999999");
    }

    @Test
    void dovrebbeRifiutareValoreUnitarioNegativo() {
        assertThatThrownBy(() -> new RigaOrdineVendita(
                10L, prodotto(), 1, new BigDecimal("-0.01")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valore unitario");
    }

    @Test
    void dovrebbeRifiutareTroppiDecimaliNelValoreUnitario() {
        assertThatThrownBy(() -> new RigaOrdineVendita(
                10L, prodotto(), 1, new BigDecimal("10.001")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2 decimali");
    }

    private Prodotto prodotto() {
        return new Prodotto(1L, "P001", "Prodotto",
                new BigDecimal("5.00"), new BigDecimal("10.20"),
                10, 1, false);
    }
}
