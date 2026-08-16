package com.gestioneOrdini.application.prodotto.usecase;

import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import com.gestioneOrdini.domain.prodotto.repository.ProdottoRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test del caso d'uso {@link GetProdottoUseCase}.
 * <p>
 * Verifica che:
 * <ul>
 *     <li>il prodotto venga correttamente recuperato quando esiste</li>
 *     <li>venga sollevata un'eccezione quando il prodotto non esiste</li>
 * </ul>
 */
class GetProdottoUseCaseTest {

    private ProdottoRepository repository;
    private GetProdottoUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(ProdottoRepository.class);
        useCase = new GetProdottoUseCase(repository);
    }

    @Test
    void deveRestituireProdottoQuandoEsiste() {

        Long id = 1L;

        // Given: prodotto presente nel repository
        Prodotto prodotto = new Prodotto(
                id,
                "P001",
                "Prodotto di test",
                new BigDecimal("10.50"),
                new BigDecimal("15.90"),
                100,
                10,
                false
        );

        when(repository.findById(id)).thenReturn(Optional.of(prodotto));

        // When
        Prodotto risultato = useCase.eseguire(id);

        // Then
        assertNotNull(risultato);
        assertEquals(id, risultato.getId());
        assertEquals("P001", risultato.getCodice());
        assertEquals("Prodotto di test", risultato.getDescrizione());
        assertEquals(new BigDecimal("10.50"), risultato.getValoreAcquisto());
        assertEquals(new BigDecimal("15.90"), risultato.getValoreVendita());
        assertEquals(100, risultato.getQuantita());
        assertEquals(10, risultato.getScortaMinima());
        assertFalse(risultato.getArchiviato());

        verify(repository, times(1)).findById(id);
    }

    @Test
    void deveLanciareEccezioneQuandoProdottoNonEsiste() {

        Long id = 999L;

        // Given: repository non trova il prodotto
        when(repository.findById(id)).thenReturn(Optional.empty());

        // When + Then
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> useCase.eseguire(id)
        );

        assertEquals("Prodotto con id " + id + " non trovato", ex.getMessage());

        verify(repository, times(1)).findById(id);
    }
}
