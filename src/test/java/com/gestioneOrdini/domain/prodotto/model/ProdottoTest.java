package com.gestioneOrdini.domain.prodotto.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ProdottoTest {

    @Test
    void dovrebbeCreareProdottoConDatiValidi() {
        Prodotto prodotto = nuovoProdotto();

        assertThat(prodotto.getId()).isNull();
        assertThat(prodotto.getCodice()).isEqualTo("P001");
        assertThat(prodotto.getDescrizione()).isEqualTo("Notebook Dell");
        assertThat(prodotto.getValoreAcquisto()).isEqualByComparingTo("1500.00");
        assertThat(prodotto.getValoreVendita()).isEqualByComparingTo("2200.00");
        assertThat(prodotto.getQuantita()).isEqualTo(10);
        assertThat(prodotto.getScortaMinima()).isEqualTo(2);
        assertThat(prodotto.getArchiviato()).isFalse();
        assertThat(prodotto.getDataRegistrazione()).isNull();
    }

    @Test
    void dovrebbeRifiutareCodiceNulloVuotoOSoloSpazi() {
        assertThatCodiceNonValidoVengaRifiutato(null);
        assertThatCodiceNonValidoVengaRifiutato("");
        assertThatCodiceNonValidoVengaRifiutato("   ");
    }

    @Test
    void dovrebbeRifiutareDescrizioneNullaVuotaOSoloSpazi() {
        assertThatDescrizioneNonValidaVengaRifiutata(null);
        assertThatDescrizioneNonValidaVengaRifiutata("");
        assertThatDescrizioneNonValidaVengaRifiutata("   ");
    }

    @Test
    void dovrebbeAccettareCodiceEDescrizioneAlLimiteMassimo() {
        Prodotto prodotto = new Prodotto(
                "ABC123",
                "D".repeat(30),
                new BigDecimal("10.00"),
                new BigDecimal("15.00"),
                1,
                0,
                false
        );

        assertThat(prodotto.getCodice()).hasSize(6);
        assertThat(prodotto.getDescrizione()).hasSize(30);
    }

    @Test
    void dovrebbeRifiutareCodiceODescrizioneOltreLaLunghezzaMassima() {
        assertThatCodiceNonValidoVengaRifiutato("ABC1234");
        assertThatDescrizioneNonValidaVengaRifiutata("D".repeat(31));
    }

    @Test
    void dovrebbeRifiutareValoriDiAcquistoOVenditaNulliONegativi() {
        assertThatValoreAcquistoNonValidoVengaRifiutato(null);
        assertThatValoreAcquistoNonValidoVengaRifiutato(new BigDecimal("-0.01"));
        assertThatValoreVenditaNonValidoVengaRifiutato(null);
        assertThatValoreVenditaNonValidoVengaRifiutato(new BigDecimal("-0.01"));
    }

    @Test
    void dovrebbeRifiutareQuantitaOScortaMinimaNulleONegative() {
        assertThatQuantitaNonValidaVengaRifiutata(null);
        assertThatQuantitaNonValidaVengaRifiutata(-1);
        assertThatScortaMinimaNonValidaVengaRifiutata(null);
        assertThatScortaMinimaNonValidaVengaRifiutata(-1);
    }

    @Test
    void dovrebbeAccettareValoriEQuantitaPariAZero() {
        Prodotto prodotto = new Prodotto(
                "P001",
                "Prodotto gratuito",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0,
                0,
                false
        );

        assertThat(prodotto.getValoreAcquisto()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(prodotto.getValoreVendita()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(prodotto.getQuantita()).isZero();
        assertThat(prodotto.getScortaMinima()).isZero();
    }

    @Test
    void dovrebbeAggiornareDatiValidiSenzaModificareCodice() {
        Prodotto prodotto = nuovoProdotto();

        prodotto.setDescrizione("Notebook Dell aggiornato");
        prodotto.setValoreAcquisto(new BigDecimal("1600.00"));
        prodotto.setValoreVendita(new BigDecimal("2300.00"));
        prodotto.setQuantita(15);
        prodotto.setScortaMinima(3);
        prodotto.setArchiviato(true);

        assertThat(prodotto.getCodice()).isEqualTo("P001");
        assertThat(prodotto.getDescrizione()).isEqualTo("Notebook Dell aggiornato");
        assertThat(prodotto.getValoreAcquisto()).isEqualByComparingTo("1600.00");
        assertThat(prodotto.getValoreVendita()).isEqualByComparingTo("2300.00");
        assertThat(prodotto.getQuantita()).isEqualTo(15);
        assertThat(prodotto.getScortaMinima()).isEqualTo(3);
        assertThat(prodotto.getArchiviato()).isTrue();
    }

    @Test
    void dovrebbeRifiutareValoriNonValidiDuranteAggiornamento() {
        Prodotto prodotto = nuovoProdotto();

        assertThatIllegalArgumentException().isThrownBy(() -> prodotto.setDescrizione(""));
        assertThatIllegalArgumentException().isThrownBy(() -> prodotto.setValoreAcquisto(new BigDecimal("-1")));
        assertThatIllegalArgumentException().isThrownBy(() -> prodotto.setValoreVendita(new BigDecimal("-1")));
        assertThatIllegalArgumentException().isThrownBy(() -> prodotto.setQuantita(-1));
        assertThatIllegalArgumentException().isThrownBy(() -> prodotto.setScortaMinima(-1));
    }

    private Prodotto nuovoProdotto() {
        return new Prodotto(
                "P001",
                "Notebook Dell",
                new BigDecimal("1500.00"),
                new BigDecimal("2200.00"),
                10,
                2,
                false
        );
    }

    private void assertThatCodiceNonValidoVengaRifiutato(String codice) {
        assertThatIllegalArgumentException().isThrownBy(() -> new Prodotto(
                codice,
                "Notebook Dell",
                new BigDecimal("1500.00"),
                new BigDecimal("2200.00"),
                10,
                2,
                false
        ));
    }

    private void assertThatDescrizioneNonValidaVengaRifiutata(String descrizione) {
        assertThatIllegalArgumentException().isThrownBy(() -> new Prodotto(
                "P001",
                descrizione,
                new BigDecimal("1500.00"),
                new BigDecimal("2200.00"),
                10,
                2,
                false
        ));
    }

    private void assertThatValoreAcquistoNonValidoVengaRifiutato(BigDecimal valoreAcquisto) {
        assertThatIllegalArgumentException().isThrownBy(() -> new Prodotto(
                "P001", "Notebook Dell", valoreAcquisto, new BigDecimal("2200.00"), 10, 2, false));
    }

    private void assertThatValoreVenditaNonValidoVengaRifiutato(BigDecimal valoreVendita) {
        assertThatIllegalArgumentException().isThrownBy(() -> new Prodotto(
                "P001", "Notebook Dell", new BigDecimal("1500.00"), valoreVendita, 10, 2, false));
    }

    private void assertThatQuantitaNonValidaVengaRifiutata(Integer quantita) {
        assertThatIllegalArgumentException().isThrownBy(() -> new Prodotto(
                "P001", "Notebook Dell", new BigDecimal("1500.00"), new BigDecimal("2200.00"), quantita, 2, false));
    }

    private void assertThatScortaMinimaNonValidaVengaRifiutata(Integer scortaMinima) {
        assertThatIllegalArgumentException().isThrownBy(() -> new Prodotto(
                "P001", "Notebook Dell", new BigDecimal("1500.00"), new BigDecimal("2200.00"), 10, scortaMinima, false));
    }
}
