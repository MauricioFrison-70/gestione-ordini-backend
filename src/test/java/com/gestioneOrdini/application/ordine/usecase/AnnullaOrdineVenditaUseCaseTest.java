package com.gestioneOrdini.application.ordine.usecase;

import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import com.gestioneOrdini.domain.ordine.model.OrdineVendita;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnnullaOrdineVenditaUseCaseTest {
    @Mock private OrdineVenditaRepository repository;

    @Test
    void dovrebbeAnnullareOrdinePendente() {
        OrdineVendita ordine = new OrdineVendita(10L, "OV-2026-000010",
                agente(1L, TipoAgente.CLIENTE), agente(2L, TipoAgente.VENDITORE),
                agente(3L, TipoAgente.TRASPORTATORE), LocalDateTime.now(), null, null);
        when(repository.findById(10L)).thenReturn(Optional.of(ordine));
        when(repository.save(ordine)).thenReturn(ordine);

        OrdineVendita risultato = new AnnullaOrdineVenditaUseCase(repository).eseguire(10L);

        assertThat(risultato.getDataAnnullamento()).isEqualTo(LocalDate.now());
        verify(repository).save(ordine);
    }

    private Agente agente(Long id, TipoAgente tipo) {
        return new Agente(id, tipo.name(), tipo.name().toLowerCase() + "@example.com", tipo, false);
    }
}
