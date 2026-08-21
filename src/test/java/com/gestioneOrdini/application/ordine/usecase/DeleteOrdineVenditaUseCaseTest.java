package com.gestioneOrdini.application.ordine.usecase;

import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import com.gestioneOrdini.domain.ordine.exception.OrdineVenditaRilasciatoException;
import com.gestioneOrdini.domain.ordine.model.OrdineVendita;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DeleteOrdineVenditaUseCaseTest {
    private final OrdineVenditaRepository repository = mock(OrdineVenditaRepository.class);
    private final DeleteOrdineVenditaUseCase useCase = new DeleteOrdineVenditaUseCase(repository);

    @Test
    void deveEliminareOrdineSenzaDataRilascio() {
        when(repository.findById(1L)).thenReturn(Optional.of(ordine(null)));

        useCase.eseguire(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void nonDeveEliminareOrdineConDataRilascio() {
        when(repository.findById(1L)).thenReturn(Optional.of(ordine(LocalDate.of(2026, 8, 25))));

        assertThrows(OrdineVenditaRilasciatoException.class, () -> useCase.eseguire(1L));

        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    void deveRifiutareOrdineInesistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> useCase.eseguire(99L));
    }

    private OrdineVendita ordine(LocalDate dataRilascio) {
        return new OrdineVendita(1L, "OV-2026-000001",
                agente(1L, TipoAgente.CLIENTE), agente(2L, TipoAgente.VENDITORE),
                agente(3L, TipoAgente.TRASPORTATORE), LocalDateTime.now(), dataRilascio);
    }

    private Agente agente(Long id, TipoAgente tipo) {
        return new Agente(id, tipo.name(), tipo.name().toLowerCase() + "@example.com", tipo, false);
    }
}
