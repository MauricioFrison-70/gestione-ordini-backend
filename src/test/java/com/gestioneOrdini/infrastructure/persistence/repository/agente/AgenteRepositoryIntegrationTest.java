package com.gestioneOrdini.infrastructure.persistence.repository.agente;

import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import com.gestioneOrdini.domain.agente.repository.AgenteRepository;
import com.gestioneOrdini.infrastructure.config.AbstractSqlServerIntegrationTest;
import com.gestioneOrdini.infrastructure.persistence.entity.AgenteEntity;
import com.gestioneOrdini.infrastructure.persistence.repository.AgenteJpaRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("integration")
@Transactional
class AgenteRepositoryIntegrationTest extends AbstractSqlServerIntegrationTest {

    @Autowired
    private AgenteRepository repository;

    @Autowired
    private AgenteJpaRepository jpaRepository;

    private Agente nuovoAgente() {
        return new Agente(
                "Mario Rossi",
                "mario.rossi@example.com",
                TipoAgente.CLIENTE,
                false
        );
    }

    @Test
    void dovrebbeSalvareERecuperareAgente() {
        Agente salvato = repository.save(nuovoAgente());

        Agente trovato = repository.findById(salvato.getId()).orElseThrow();

        assertThat(salvato.getId()).isNotNull();
        assertThat(trovato.getNome()).isEqualTo("Mario Rossi");
        assertThat(trovato.getEmail()).isEqualTo("mario.rossi@example.com");
        assertThat(trovato.getTipoAgente()).isEqualTo(TipoAgente.CLIENTE);
        assertThat(trovato.getArchiviato()).isFalse();
        assertThat(trovato.getDataRegistrazione()).isNotNull();
    }

    @Test
    void dovrebbeRestituireVuotoQuandoAgenteNonEsiste() {
        assertThat(repository.findById(Long.MAX_VALUE)).isEmpty();
    }

    @Test
    void dovrebbeElencareTuttiGliAgentiSalvati() {
        Agente primeiro = repository.save(nuovoAgente());
        Agente segundo = repository.save(new Agente(
                "Luigi Bianchi",
                "luigi.bianchi@example.com",
                TipoAgente.VENDITORE,
                false
        ));

        assertThat(repository.findAll())
                .extracting(Agente::getId)
                .containsExactlyInAnyOrder(primeiro.getId(), segundo.getId());
    }

    @Test
    void dovrebbeAggiornareAgenteEsistente() {
        Agente salvo = repository.save(nuovoAgente());
        var dataRegistrazioneOriginale = salvo.getDataRegistrazione();
        salvo.setNome("Mario Bianchi");
        salvo.setEmail("mario.bianchi@example.com");
        salvo.setTipoAgente(TipoAgente.VENDITORE);
        salvo.setArchiviato(true);

        Agente atualizado = repository.save(salvo);
        Agente encontrado = repository.findById(salvo.getId()).orElseThrow();

        assertThat(atualizado.getId()).isEqualTo(salvo.getId());
        assertThat(encontrado.getNome()).isEqualTo("Mario Bianchi");
        assertThat(encontrado.getEmail()).isEqualTo("mario.bianchi@example.com");
        assertThat(encontrado.getTipoAgente()).isEqualTo(TipoAgente.VENDITORE);
        assertThat(encontrado.getArchiviato()).isTrue();
        assertThat(encontrado.getDataRegistrazione()).isEqualTo(dataRegistrazioneOriginale);
    }

    @Test
    void dovrebbeEliminareAgentePerId() {
        Agente salvo = repository.save(nuovoAgente());

        repository.deleteById(salvo.getId());

        assertThat(repository.existsById(salvo.getId())).isFalse();
        assertThat(repository.findById(salvo.getId())).isEmpty();
    }

    @Test
    void dovrebbeIndicareSeAgenteEsiste() {
        Agente salvo = repository.save(nuovoAgente());

        assertThat(repository.existsById(salvo.getId())).isTrue();
        assertThat(repository.existsById(Long.MAX_VALUE)).isFalse();
    }

    @Test
    void dovrebbeRifiutareAgenteSenzaNome() {
        AgenteEntity agente = new AgenteEntity(
                null,
                null,
                "mario.rossi@example.com",
                TipoAgente.CLIENTE,
                false
        );

        assertThatVincoloDelDatabaseVengaViolato(agente);
    }

    @Test
    void dovrebbeRifiutareAgenteSenzaEmail() {
        AgenteEntity agente = new AgenteEntity(
                null,
                "Mario Rossi",
                null,
                TipoAgente.CLIENTE,
                false
        );

        assertThatVincoloDelDatabaseVengaViolato(agente);
    }

    @Test
    void dovrebbeRifiutareAgenteSenzaTipo() {
        AgenteEntity agente = new AgenteEntity(
                null,
                "Mario Rossi",
                "mario.rossi@example.com",
                null,
                false
        );

        assertThatVincoloDelDatabaseVengaViolato(agente);
    }

    @Test
    void dovrebbeRifiutareAgenteSenzaStatoDiArchiviazione() {
        AgenteEntity agente = new AgenteEntity(
                null,
                "Mario Rossi",
                "mario.rossi@example.com",
                TipoAgente.CLIENTE,
                null
        );

        assertThatVincoloDelDatabaseVengaViolato(agente);
    }

    @Test
    void dovrebbeRifiutareNomePiuLungoDiSessantaCaratteri() {
        AgenteEntity agente = new AgenteEntity(
                null,
                "A".repeat(61),
                "mario.rossi@example.com",
                TipoAgente.CLIENTE,
                false
        );

        assertThatVincoloDelDatabaseVengaViolato(agente);
    }

    @Test
    void dovrebbeRifiutareEmailPiuLungaDiOttantaCaratteri() {
        AgenteEntity agente = new AgenteEntity(
                null,
                "Mario Rossi",
                "a".repeat(70) + "@example.com",
                TipoAgente.CLIENTE,
                false
        );

        assertThatVincoloDelDatabaseVengaViolato(agente);
    }

    private void assertThatVincoloDelDatabaseVengaViolato(AgenteEntity agente) {
        assertThatThrownBy(() -> {
            jpaRepository.save(agente);
            jpaRepository.flush();
        }).isInstanceOf(DataAccessException.class);
    }
}
