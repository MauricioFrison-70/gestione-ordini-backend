package com.gestioneOrdini.domain.agente.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class AgenteTest {

    @Test
    void dovrebbeCreareAgenteConDatiValidi() {
        Agente agente = nuovoAgente();

        assertThat(agente.getId()).isNull();
        assertThat(agente.getNome()).isEqualTo("Mario Rossi");
        assertThat(agente.getEmail()).isEqualTo("mario.rossi@example.com");
        assertThat(agente.getTipoAgente()).isEqualTo(TipoAgente.CLIENTE);
        assertThat(agente.getArchiviato()).isFalse();
        assertThat(agente.getDataRegistrazione()).isNull();
    }

    @Test
    void dovrebbeRifiutareNomeNulloVuotoOSoloSpazi() {
        assertThatNomeNonValidoVengaRifiutato(null);
        assertThatNomeNonValidoVengaRifiutato("");
        assertThatNomeNonValidoVengaRifiutato("   ");
    }

    @Test
    void dovrebbeRifiutareNomePiuLungoDiSessantaCaratteri() {
        assertThatNomeNonValidoVengaRifiutato("A".repeat(61));
    }

    @Test
    void dovrebbeAccettareNomeDiSessantaCaratteri() {
        Agente agente = new Agente(
                "A".repeat(60),
                "mario.rossi@example.com",
                TipoAgente.CLIENTE,
                false
        );

        assertThat(agente.getNome()).hasSize(60);
    }

    @Test
    void dovrebbeRifiutareEmailNullaVuotaOSoloSpazi() {
        assertThatEmailNonValidaVengaRifiutata(null);
        assertThatEmailNonValidaVengaRifiutata("");
        assertThatEmailNonValidaVengaRifiutata("   ");
    }

    @Test
    void dovrebbeRifiutareEmailConFormatoNonValido() {
        assertThatEmailNonValidaVengaRifiutata("mario.rossi");
        assertThatEmailNonValidaVengaRifiutata("mario@rossi");
        assertThatEmailNonValidaVengaRifiutata("mario rossi@example.com");
    }

    @Test
    void dovrebbeRifiutareEmailPiuLungaDiOttantaCaratteri() {
        assertThatEmailNonValidaVengaRifiutata("a".repeat(70) + "@example.com");
    }

    @Test
    void dovrebbeRifiutareTipoAgenteNullo() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Agente(
                "Mario Rossi",
                "mario.rossi@example.com",
                null,
                false
        ));
    }

    @Test
    void dovrebbeAggiornareDatiConValoriValidi() {
        Agente agente = nuovoAgente();

        agente.setNome("Luigi Bianchi");
        agente.setEmail("luigi.bianchi@example.com");
        agente.setTipoAgente(TipoAgente.FORNITORE);
        agente.setArchiviato(true);

        assertThat(agente.getNome()).isEqualTo("Luigi Bianchi");
        assertThat(agente.getEmail()).isEqualTo("luigi.bianchi@example.com");
        assertThat(agente.getTipoAgente()).isEqualTo(TipoAgente.FORNITORE);
        assertThat(agente.getArchiviato()).isTrue();
    }

    @Test
    void dovrebbeRifiutareValoriNonValidiDuranteAggiornamento() {
        Agente agente = nuovoAgente();

        assertThatIllegalArgumentException().isThrownBy(() -> agente.setNome(""));
        assertThatIllegalArgumentException().isThrownBy(() -> agente.setEmail("email-non-valida"));
        assertThatIllegalArgumentException().isThrownBy(() -> agente.setTipoAgente(null));
    }

    private Agente nuovoAgente() {
        return new Agente(
                "Mario Rossi",
                "mario.rossi@example.com",
                TipoAgente.CLIENTE,
                false
        );
    }

    private void assertThatNomeNonValidoVengaRifiutato(String nome) {
        assertThatIllegalArgumentException().isThrownBy(() -> new Agente(
                nome,
                "mario.rossi@example.com",
                TipoAgente.CLIENTE,
                false
        ));
    }

    private void assertThatEmailNonValidaVengaRifiutata(String email) {
        assertThatIllegalArgumentException().isThrownBy(() -> new Agente(
                "Mario Rossi",
                email,
                TipoAgente.CLIENTE,
                false
        ));
    }
}
