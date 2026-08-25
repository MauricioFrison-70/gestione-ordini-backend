package com.gestioneOrdini.application.agente.usecase;

import com.gestioneOrdini.domain.agente.exception.AgenteUtilizzatoException;
import com.gestioneOrdini.domain.agente.repository.AgenteRepository;
import com.gestioneOrdini.domain.acquisto.repository.OrdineAcquistoRepository;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Test del caso d'uso {@link DeleteAgenteUseCase}.
 * <p>
 * Verifica che:
 * <ul>
 *     <li>l'agente venga eliminato correttamente quando esiste</li>
 *     <li>venga sollevata un'eccezione quando l'agente non esiste</li>
 * </ul>
 */
class DeleteAgenteUseCaseTest {

    private AgenteRepository repository;
    private OrdineVenditaRepository ordineVenditaRepository;
    private OrdineAcquistoRepository ordineAcquistoRepository;
    private DeleteAgenteUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(AgenteRepository.class);
        ordineVenditaRepository = mock(OrdineVenditaRepository.class);
        ordineAcquistoRepository = mock(OrdineAcquistoRepository.class);
        useCase = new DeleteAgenteUseCase(
                repository, ordineVenditaRepository, ordineAcquistoRepository);
    }

    @Test
    void deveEliminareAgenteQuandoEsiste() {

        Long id = 1L;

        // Given: il repository conferma che l'agente esiste
        when(repository.existsById(id)).thenReturn(true);

        // When
        useCase.eseguire(id);

        // Then: verifica che la cancellazione sia stata eseguita
        verify(repository, times(1)).existsById(id);
        verify(repository, times(1)).deleteById(id);
    }

    @Test
    void deveLanciareEccezioneQuandoAgenteNonEsiste() {

        Long id = 999L;

        // Given: il repository indica che l'agente NON esiste
        when(repository.existsById(id)).thenReturn(false);

        // When + Then: deve sollevare EntityNotFoundException
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> useCase.eseguire(id)
        );

        assertEquals("Agente con id " + id + " non trovato", ex.getMessage());

        // Verifica che deleteById NON sia stato chiamato
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    void deveImpedireEliminazioneQuandoAgenteUtilizzatoInOrdine() {
        when(repository.existsById(1L)).thenReturn(true);
        when(ordineVenditaRepository.existsByAgenteId(1L)).thenReturn(true);

        assertThrows(AgenteUtilizzatoException.class, () -> useCase.eseguire(1L));

        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    void deveImpedireEliminazioneQuandoFornitoreUtilizzatoInAcquisto() {
        when(repository.existsById(1L)).thenReturn(true);
        when(ordineAcquistoRepository.existsByFornitoreId(1L)).thenReturn(true);

        assertThrows(AgenteUtilizzatoException.class, () -> useCase.eseguire(1L));

        verify(repository, never()).deleteById(anyLong());
    }
}
