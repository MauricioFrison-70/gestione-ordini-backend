package com.gestioneOrdini.application.prodotto.usecase;

import com.gestioneOrdini.domain.prodotto.repository.ProdottoRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Test del caso d'uso {@link DeleteProdottoUseCase}.
 * <p>
 * Verifica che:
 * <ul>
 *     <li>il prodotto venga eliminato correttamente quando esiste</li>
 *     <li>venga sollevata un'eccezione quando il prodotto non esiste</li>
 * </ul>
 */
class DeleteProdottoUseCaseTest {

    private ProdottoRepository repository;
    private DeleteProdottoUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(ProdottoRepository.class);
        useCase = new DeleteProdottoUseCase(repository);
    }

    @Test
    void deveEliminareProdottoQuandoEsiste() {

        Long id = 1L;

        // Given: il repository conferma che il prodotto esiste
        when(repository.existsById(id)).thenReturn(true);

        // When
        useCase.eseguire(id);

        // Then: verifica che la cancellazione sia stata eseguita
        verify(repository, times(1)).existsById(id);
        verify(repository, times(1)).deleteById(id);
    }

    @Test
    void deveLanciareEccezioneQuandoProdottoNonEsiste() {

        Long id = 999L;

        // Given: il repository indica che il prodotto NON esiste
        when(repository.existsById(id)).thenReturn(false);

        // When + Then: deve sollevare EntityNotFoundException
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> useCase.eseguire(id)
        );

        assertEquals("Prodotto con id " + id + " non trovato", ex.getMessage());

        // Verifica che deleteById NON sia stato chiamato
        verify(repository, never()).deleteById(anyLong());
    }
}
