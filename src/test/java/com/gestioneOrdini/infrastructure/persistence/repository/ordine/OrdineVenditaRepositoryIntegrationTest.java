package com.gestioneOrdini.infrastructure.persistence.repository.ordine;

import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import com.gestioneOrdini.domain.agente.repository.AgenteRepository;
import com.gestioneOrdini.domain.ordine.model.OrdineVendita;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import com.gestioneOrdini.infrastructure.config.AbstractSqlServerIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@Transactional
class OrdineVenditaRepositoryIntegrationTest extends AbstractSqlServerIntegrationTest {
    @Autowired private OrdineVenditaRepository ordineRepository;
    @Autowired private AgenteRepository agenteRepository;

    @Test
    void dovrebbeSalvareNumeroAutomaticoRelazioniEDate() {
        Agente cliente = salva("Cliente", TipoAgente.CLIENTE);
        Agente venditore = salva("Venditore", TipoAgente.VENDITORE);
        Agente trasportatore = salva("Trasportatore", TipoAgente.TRASPORTATORE);
        LocalDate rilascio = LocalDate.of(2026, 8, 22);

        OrdineVendita salvato = ordineRepository.save(
                new OrdineVendita(cliente, venditore, trasportatore, rilascio));
        OrdineVendita trovato = ordineRepository.findById(salvato.getId()).orElseThrow();

        assertThat(trovato.getNumeroOrdine())
                .isEqualTo("OV-%d-%06d".formatted(trovato.getDataRegistrazione().getYear(), trovato.getId()));
        assertThat(trovato.getCliente().getId()).isEqualTo(cliente.getId());
        assertThat(trovato.getVenditore().getId()).isEqualTo(venditore.getId());
        assertThat(trovato.getTrasportatore().getId()).isEqualTo(trasportatore.getId());
        assertThat(trovato.getDataRegistrazione()).isNotNull();
        assertThat(trovato.getDataRilascio()).isEqualTo(rilascio);
        assertThat(trovato.getDataAnnullamento()).isNull();
        assertThat(ordineRepository.existsByAgenteId(cliente.getId())).isTrue();
        assertThat(ordineRepository.existsByAgenteId(venditore.getId())).isTrue();
        assertThat(ordineRepository.existsByAgenteId(trasportatore.getId())).isTrue();
    }

    @Test
    void dovrebbeSalvareDataAnnullamentoSenzaDataRilascio() {
        Agente cliente = salva("Cliente Annullato", TipoAgente.CLIENTE);
        Agente venditore = salva("Venditore Annullato", TipoAgente.VENDITORE);
        Agente trasportatore = salva("Trasportatore Annullato", TipoAgente.TRASPORTATORE);
        LocalDate annullamento = LocalDate.of(2026, 8, 23);
        OrdineVendita ordine = new OrdineVendita(cliente, venditore, trasportatore, null);
        ordine.annulla(annullamento);

        OrdineVendita salvato = ordineRepository.save(ordine);
        OrdineVendita trovato = ordineRepository.findById(salvato.getId()).orElseThrow();

        assertThat(trovato.getDataRilascio()).isNull();
        assertThat(trovato.getDataAnnullamento()).isEqualTo(annullamento);
    }

    @Test
    void dovrebbeGenerareNumeriSequenzialiUnivoci() {
        Agente cliente = salva("Cliente Due", TipoAgente.CLIENTE);
        Agente venditore = salva("Venditore Due", TipoAgente.VENDITORE);
        Agente trasportatore = salva("Trasportatore Due", TipoAgente.TRASPORTATORE);

        OrdineVendita primo = ordineRepository.save(new OrdineVendita(cliente, venditore, trasportatore, null));
        OrdineVendita secondo = ordineRepository.save(new OrdineVendita(cliente, venditore, trasportatore, null));

        assertThat(secondo.getId()).isGreaterThan(primo.getId());
        assertThat(secondo.getNumeroOrdine()).isNotEqualTo(primo.getNumeroOrdine());
    }

    @Test
    void dovrebbeEliminareOrdinePerId() {
        Agente cliente = salva("Cliente Tre", TipoAgente.CLIENTE);
        Agente venditore = salva("Venditore Tre", TipoAgente.VENDITORE);
        Agente trasportatore = salva("Trasportatore Tre", TipoAgente.TRASPORTATORE);
        OrdineVendita salvato = ordineRepository.save(
                new OrdineVendita(cliente, venditore, trasportatore, null));

        ordineRepository.deleteById(salvato.getId());

        assertThat(ordineRepository.findById(salvato.getId())).isEmpty();
    }

    private Agente salva(String nome, TipoAgente tipo) {
        return agenteRepository.save(new Agente(nome, nome.toLowerCase().replace(" ", "")
                + "@example.com", tipo, false));
    }
}
