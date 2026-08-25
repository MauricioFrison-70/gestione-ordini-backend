package com.gestioneOrdini.application.agente.usecase;

import com.gestioneOrdini.domain.agente.repository.AgenteRepository;
import com.gestioneOrdini.domain.acquisto.repository.OrdineAcquistoRepository;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CheckAgenteUtilizzatoUseCaseTest {
    private final AgenteRepository agenteRepository = mock(AgenteRepository.class);
    private final OrdineVenditaRepository ordineRepository = mock(OrdineVenditaRepository.class);
    private final OrdineAcquistoRepository ordineAcquistoRepository =
            mock(OrdineAcquistoRepository.class);
    private final CheckAgenteUtilizzatoUseCase useCase =
            new CheckAgenteUtilizzatoUseCase(
                    agenteRepository, ordineRepository, ordineAcquistoRepository);

    @Test
    void deveIndicareQuandoAgenteUtilizzato() {
        when(agenteRepository.existsById(1L)).thenReturn(true);
        when(ordineRepository.existsByAgenteId(1L)).thenReturn(true);

        assertTrue(useCase.eseguire(1L));
    }

    @Test
    void deveIndicareQuandoAgenteNonUtilizzato() {
        when(agenteRepository.existsById(1L)).thenReturn(true);

        assertFalse(useCase.eseguire(1L));
    }

    @Test
    void deveIndicareQuandoFornitoreUtilizzatoInAcquisto() {
        when(agenteRepository.existsById(1L)).thenReturn(true);
        when(ordineAcquistoRepository.existsByFornitoreId(1L)).thenReturn(true);

        assertTrue(useCase.eseguire(1L));
    }

    @Test
    void deveRifiutareAgenteInesistente() {
        when(agenteRepository.existsById(99L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> useCase.eseguire(99L));
        verifyNoInteractions(ordineRepository);
    }
}
