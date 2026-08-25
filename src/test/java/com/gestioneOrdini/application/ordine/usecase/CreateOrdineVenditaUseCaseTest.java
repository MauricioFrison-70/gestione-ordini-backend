package com.gestioneOrdini.application.ordine.usecase;

import com.gestioneOrdini.application.ordine.dto.OrdineVenditaRequest;
import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import com.gestioneOrdini.domain.agente.repository.AgenteRepository;
import com.gestioneOrdini.domain.ordine.model.OrdineVendita;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrdineVenditaUseCaseTest {
    @Mock private OrdineVenditaRepository ordineRepository;
    @Mock private AgenteRepository agenteRepository;
    private CreateOrdineVenditaUseCase useCase;

    @BeforeEach
    void setUp() { useCase = new CreateOrdineVenditaUseCase(ordineRepository, agenteRepository); }

    @Test
    void dovrebbeCreareOrdineConAgentiValidi() {
        var cliente = agente(1L, TipoAgente.CLIENTE);
        var venditore = agente(2L, TipoAgente.VENDITORE);
        var trasportatore = agente(3L, TipoAgente.TRASPORTATORE);
        when(agenteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(agenteRepository.findById(2L)).thenReturn(Optional.of(venditore));
        when(agenteRepository.findById(3L)).thenReturn(Optional.of(trasportatore));
        when(ordineRepository.save(any())).thenAnswer(invocation -> {
            OrdineVendita o = invocation.getArgument(0);
            return new OrdineVendita(1L, "OV-2026-000001", o.getCliente(), o.getVenditore(),
                    o.getTrasportatore(), LocalDateTime.now(), o.getDataRilascio(),
                    o.getDataAnnullamento());
        });

        var salvato = useCase.eseguire(new OrdineVenditaRequest(1L, 2L, 3L));

        assertThat(salvato.getNumeroOrdine()).isEqualTo("OV-2026-000001");
        assertThat(salvato.getDataAnnullamento()).isNull();
        verify(ordineRepository).save(any(OrdineVendita.class));
    }

    @Test
    void dovrebbeRifiutareAgenteConTipoIncompatibile() {
        when(agenteRepository.findById(1L)).thenReturn(Optional.of(agente(1L, TipoAgente.FORNITORE)));
        when(agenteRepository.findById(2L)).thenReturn(Optional.of(agente(2L, TipoAgente.VENDITORE)));
        when(agenteRepository.findById(3L)).thenReturn(Optional.of(agente(3L, TipoAgente.TRASPORTATORE)));

        assertThatThrownBy(() -> useCase.eseguire(new OrdineVenditaRequest(1L, 2L, 3L)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("CLIENTE");
        verifyNoInteractions(ordineRepository);
    }

    @Test
    void dovrebbeRitornareNotFoundQuandoAgenteNonEsiste() {
        when(agenteRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.eseguire(new OrdineVenditaRequest(99L, 2L, 3L)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    private Agente agente(Long id, TipoAgente tipo) {
        return new Agente(id, tipo.name(), tipo.name().toLowerCase() + "@example.com", tipo, false);
    }
}
