package com.gestioneOrdini.infrastructure.persistence.repository.prodotto;

import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import com.gestioneOrdini.domain.prodotto.repository.ProdottoRepository;
import com.gestioneOrdini.infrastructure.config.AbstractSqlServerIntegrationTest;
import com.gestioneOrdini.infrastructure.persistence.repository.ProdottoJpaRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.gestioneOrdini.domain.prodotto.exception.CodiceProdottoDuplicatoException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("integration")
@Transactional
class ProdottoRepositoryIntegrationTest extends AbstractSqlServerIntegrationTest {

    @Autowired
    private ProdottoRepository repository;

    @Autowired
    private ProdottoJpaRepository jpaRepository;

    @Test
    void dovrebbeSalvareERecuperareProdotto() {
        Prodotto salvato = repository.save(nuovoProdotto("P001"));

        Prodotto trovato = repository.findById(salvato.getId()).orElseThrow();
        Prodotto trovatoPerCodice = repository.findByCodice("P001").orElseThrow();

        assertThat(salvato.getId()).isNotNull();
        assertThat(trovato.getCodice()).isEqualTo("P001");
        assertThat(trovato.getDescrizione()).isEqualTo("Notebook Dell");
        assertThat(trovato.getValoreAcquisto()).isEqualByComparingTo("1500.00");
        assertThat(trovato.getValoreVendita()).isEqualByComparingTo("2200.00");
        assertThat(trovato.getQuantita()).isEqualTo(10);
        assertThat(trovato.getScortaMinima()).isEqualTo(2);
        assertThat(trovato.getArchiviato()).isFalse();
        assertThat(trovato.getDataRegistrazione()).isNotNull();
        assertThat(trovatoPerCodice.getId()).isEqualTo(salvato.getId());
    }

    @Test
    void dovrebbeRestituireVuotoQuandoProdottoNonEsiste() {
        assertThat(repository.findById(Long.MAX_VALUE)).isEmpty();
    }

    @Test
    void dovrebbeRecuperareProdottoConBloccoPerAggiornamento() {
        Prodotto salvato = repository.save(nuovoProdotto("P003"));

        Prodotto bloccato = repository.findByIdForUpdate(salvato.getId()).orElseThrow();

        assertThat(bloccato.getId()).isEqualTo(salvato.getId());
        assertThat(bloccato.getQuantita()).isEqualTo(10);
    }

    @Test
    void dovrebbeElencareTuttiIProdottiSalvati() {
        Prodotto primo = repository.save(nuovoProdotto("P001"));
        Prodotto secondo = repository.save(nuovoProdotto("P002"));

        assertThat(repository.findAll())
                .extracting(Prodotto::getId)
                .containsExactlyInAnyOrder(primo.getId(), secondo.getId());
    }

    @Test
    void dovrebbeAggiornareProdottoSenzaModificareCodice() {
        Prodotto salvato = repository.save(nuovoProdotto("P001"));
        var dataRegistrazioneOriginale = salvato.getDataRegistrazione();
        salvato.setDescrizione("Notebook Dell aggiornato");
        salvato.setValoreAcquisto(new BigDecimal("1600.00"));
        salvato.setValoreVendita(new BigDecimal("2300.00"));
        salvato.setQuantita(15);
        salvato.setScortaMinima(3);
        salvato.setArchiviato(true);

        repository.save(salvato);
        Prodotto trovato = repository.findById(salvato.getId()).orElseThrow();

        assertThat(trovato.getCodice()).isEqualTo("P001");
        assertThat(trovato.getDescrizione()).isEqualTo("Notebook Dell aggiornato");
        assertThat(trovato.getValoreAcquisto()).isEqualByComparingTo("1600.00");
        assertThat(trovato.getValoreVendita()).isEqualByComparingTo("2300.00");
        assertThat(trovato.getQuantita()).isEqualTo(15);
        assertThat(trovato.getScortaMinima()).isEqualTo(3);
        assertThat(trovato.getArchiviato()).isTrue();
        assertThat(trovato.getDataRegistrazione()).isEqualTo(dataRegistrazioneOriginale);
    }

    @Test
    void dovrebbeEliminareProdottoPerId() {
        Prodotto salvato = repository.save(nuovoProdotto("P001"));

        repository.deleteById(salvato.getId());

        assertThat(repository.existsById(salvato.getId())).isFalse();
        assertThat(repository.findById(salvato.getId())).isEmpty();
    }

    @Test
    void dovrebbeIndicareSeProdottoEsiste() {
        Prodotto salvato = repository.save(nuovoProdotto("P001"));

        assertThat(repository.existsById(salvato.getId())).isTrue();
        assertThat(repository.existsById(Long.MAX_VALUE)).isFalse();
    }

    @Test
    void dovrebbeRifiutareCodiceGiaEsistente() {
        repository.save(nuovoProdotto("P001"));
        jpaRepository.flush();

        assertThatThrownBy(() -> {
            repository.save(nuovoProdotto("P001"));
            jpaRepository.flush();
        })
                .isInstanceOf(CodiceProdottoDuplicatoException.class);
    }

    private Prodotto nuovoProdotto(String codice) {
        return new Prodotto(
                codice,
                "Notebook Dell",
                new BigDecimal("1500.00"),
                new BigDecimal("2200.00"),
                10,
                2,
                false
        );
    }
}
