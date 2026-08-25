package com.gestioneOrdini.infrastructure.persistence.repository.ordine;

import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import com.gestioneOrdini.domain.agente.repository.AgenteRepository;
import com.gestioneOrdini.domain.ordine.exception.ProdottoGiaPresenteNellOrdineException;
import com.gestioneOrdini.domain.ordine.model.OrdineVendita;
import com.gestioneOrdini.domain.ordine.model.RigaOrdineVendita;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import com.gestioneOrdini.domain.ordine.repository.RigaOrdineVenditaRepository;
import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import com.gestioneOrdini.domain.prodotto.repository.ProdottoRepository;
import com.gestioneOrdini.infrastructure.config.AbstractSqlServerIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("integration")
@Transactional
class RigaOrdineVenditaRepositoryIntegrationTest
        extends AbstractSqlServerIntegrationTest {

    @Autowired private RigaOrdineVenditaRepository rigaRepository;
    @Autowired private OrdineVenditaRepository ordineRepository;
    @Autowired private AgenteRepository agenteRepository;
    @Autowired private ProdottoRepository prodottoRepository;

    @Test
    void dovrebbeSalvareCercareElencareEAggiornareRiga() {
        OrdineVendita ordine = salvaOrdine("A");
        Prodotto prodotto = salvaProdotto("P001");
        RigaOrdineVendita salvata = rigaRepository.save(new RigaOrdineVendita(
                ordine.getId(), prodotto,
                2, new BigDecimal("10.20")));

        RigaOrdineVendita trovata = rigaRepository
                .findByIdAndOrdineVenditaId(salvata.getId(), ordine.getId())
                .orElseThrow();

        assertThat(trovata.getProdotto().getCodice()).isEqualTo("P001");
        assertThat(trovata.getQuantita()).isEqualTo(2);
        assertThat(trovata.getValoreUnitario()).isEqualByComparingTo("10.20");
        assertThat(trovata.getTotaleRiga()).isEqualByComparingTo("20.40");
        assertThat(rigaRepository.findAllByOrdineVenditaId(ordine.getId()))
                .extracting(RigaOrdineVendita::getId)
                .containsExactly(salvata.getId());
        assertThat(rigaRepository.existsByOrdineVenditaIdAndProdottoId(
                ordine.getId(), prodotto.getId())).isTrue();

        trovata.aggiorna(prodotto, 3, new BigDecimal("11.00"));
        RigaOrdineVendita aggiornata = rigaRepository.save(trovata);
        assertThat(aggiornata.getQuantita()).isEqualTo(3);
        assertThat(aggiornata.getValoreUnitario()).isEqualByComparingTo("11.00");
    }

    @Test
    void dovrebbeIsolareRigaNelProprioOrdineEdEliminarla() {
        OrdineVendita primoOrdine = salvaOrdine("B");
        OrdineVendita secondoOrdine = salvaOrdine("C");
        Prodotto prodotto = salvaProdotto("P002");
        RigaOrdineVendita salvata = rigaRepository.save(new RigaOrdineVendita(
                primoOrdine.getId(), prodotto, 1, new BigDecimal("5.00")));

        assertThat(rigaRepository.findByIdAndOrdineVenditaId(
                salvata.getId(), secondoOrdine.getId())).isEmpty();

        rigaRepository.deleteByIdAndOrdineVenditaId(
                salvata.getId(), primoOrdine.getId());
        assertThat(rigaRepository.findByIdAndOrdineVenditaId(
                salvata.getId(), primoOrdine.getId())).isEmpty();
    }

    @Test
    void dovrebbeRifiutareProdottoDuplicatoNelloStessoOrdine() {
        OrdineVendita ordine = salvaOrdine("D");
        Prodotto prodotto = salvaProdotto("P003");
        rigaRepository.save(new RigaOrdineVendita(
                ordine.getId(), prodotto, 1, new BigDecimal("5.00")));

        assertThatThrownBy(() -> rigaRepository.save(new RigaOrdineVendita(
                ordine.getId(), prodotto, 2, new BigDecimal("6.00"))))
                .isInstanceOf(ProdottoGiaPresenteNellOrdineException.class);
    }

    private OrdineVendita salvaOrdine(String suffisso) {
        Agente cliente = salvaAgente("Cliente " + suffisso, TipoAgente.CLIENTE);
        Agente venditore = salvaAgente("Venditore " + suffisso, TipoAgente.VENDITORE);
        Agente trasportatore = salvaAgente(
                "Trasportatore " + suffisso, TipoAgente.TRASPORTATORE);
        return ordineRepository.save(new OrdineVendita(
                cliente, venditore, trasportatore, null));
    }

    private Agente salvaAgente(String nome, TipoAgente tipo) {
        return agenteRepository.save(new Agente(
                nome,
                nome.toLowerCase().replace(" ", "") + "@righe.example.com",
                tipo,
                false));
    }

    private Prodotto salvaProdotto(String codice) {
        return prodottoRepository.save(new Prodotto(
                codice, "Prodotto " + codice,
                new BigDecimal("3.00"), new BigDecimal("5.00"),
                100, 10, false));
    }
}
