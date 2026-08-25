package com.gestioneOrdini.application.ordine.usecase;

import com.gestioneOrdini.application.ordine.dto.OrdineVenditaRequest;
import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import com.gestioneOrdini.domain.agente.repository.AgenteRepository;
import com.gestioneOrdini.domain.ordine.exception.OrdineVenditaNonModificabileException;
import com.gestioneOrdini.domain.ordine.model.OrdineVendita;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateOrdineVenditaUseCaseTest {

    @Mock private OrdineVenditaRepository ordineRepository;
    @Mock private AgenteRepository agenteRepository;
    private UpdateOrdineVenditaUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateOrdineVenditaUseCase(ordineRepository, agenteRepository);
    }

    @Test
    void dovrebbeAggiornareGliAgentiQuandoOrdinePendente() {
        Agente cliente = agente(1L, TipoAgente.CLIENTE);
        Agente venditore = agente(2L, TipoAgente.VENDITORE);
        Agente trasportatore = agente(3L, TipoAgente.TRASPORTATORE);
        OrdineVendita ordine = new OrdineVendita(
                10L, "OV-2026-000010", cliente, venditore, trasportatore,
                LocalDateTime.of(2026, 8, 21, 10, 0), null, null);
        Agente nuovoCliente = agente(4L, TipoAgente.CLIENTE);

        when(ordineRepository.findById(10L)).thenReturn(Optional.of(ordine));
        when(agenteRepository.findById(4L)).thenReturn(Optional.of(nuovoCliente));
        when(agenteRepository.findById(2L)).thenReturn(Optional.of(venditore));
        when(agenteRepository.findById(3L)).thenReturn(Optional.of(trasportatore));
        when(ordineRepository.save(ordine)).thenReturn(ordine);

        OrdineVendita aggiornato = useCase.eseguire(
                10L,
                new OrdineVenditaRequest(4L, 2L, 3L)
        );

        assertThat(aggiornato.getCliente()).isSameAs(nuovoCliente);
        assertThat(aggiornato.getDataAnnullamento()).isNull();
    }

    @Test
    void dovrebbeRifiutareModificaQuandoOrdineRilasciato() {
        Agente cliente = agente(1L, TipoAgente.CLIENTE);
        Agente venditore = agente(2L, TipoAgente.VENDITORE);
        Agente trasportatore = agente(3L, TipoAgente.TRASPORTATORE);
        LocalDate dataRilascio = LocalDate.of(2026, 8, 21);
        OrdineVendita ordine = new OrdineVendita(
                10L, "OV-2026-000010", cliente, venditore, trasportatore,
                LocalDateTime.of(2026, 8, 20, 10, 0), dataRilascio, null);
        when(ordineRepository.findById(10L)).thenReturn(Optional.of(ordine));

        assertThatThrownBy(() -> useCase.eseguire(
                10L,
                new OrdineVenditaRequest(1L, 2L, 3L)))
                .isInstanceOf(OrdineVenditaNonModificabileException.class)
                .hasMessageContaining("già stato rilasciato");

        verifyNoInteractions(agenteRepository);
    }

    private Agente agente(Long id, TipoAgente tipo) {
        return new Agente(id, tipo.name(), tipo.name().toLowerCase() + "@example.com", tipo, false);
    }
}
