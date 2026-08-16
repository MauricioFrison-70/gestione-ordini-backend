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
 * Test del caso d'uso {@link UpdateProdottoUseCase}.
 * <p>
 * Verifica che:
 * <ul>
 *     <li>il prodotto venga aggiornato correttamente quando esiste</li>
 *     <li>venga sollevata un'eccezione quando il prodotto non esiste</li>
 * </ul>
 */
class UpdateProdottoUseCaseTest {

    private ProdottoRepository repository;
    private UpdateProdottoUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(ProdottoRepository.class);
        useCase = new UpdateProdottoUseCase(repository);
    }

    @Test
    void deveAggiornareProdottoQuandoEsiste() {

        Long id = 1L;

        // Prodotto esistente nel repository
        Prodotto esistente = new Prodotto(
                id,
                "P001",
                "Prodotto originale",
                new BigDecimal("10.00"),
                new BigDecimal("15.00"),
                100,
                10,
                false
        );

        // Nuovi dati aggiornati
        Prodotto aggiornato = new Prodotto(
                null,
                "P999",
                "Prodotto aggiornato",
                new BigDecimal("12.50"),
                new BigDecimal("18.90"),
                150,
                20,
                true
        );

        when(repository.findById(id)).thenReturn(Optional.of(esistente));
        when(repository.save(esistente)).thenReturn(esistente);

        // When
        Prodotto risultato = useCase.eseguire(id, aggiornato);

        // Then: verifica aggiornamento dei campi
        assertNotNull(risultato);
        assertEquals("P001", risultato.getCodice());
        assertEquals("Prodotto aggiornato", risultato.getDescrizione());
        assertEquals(new BigDecimal("12.50"), risultato.getValoreAcquisto());
        assertEquals(new BigDecimal("18.90"), risultato.getValoreVendita());
        assertEquals(150, risultato.getQuantita());
        assertEquals(20, risultato.getScortaMinima());
        assertTrue(risultato.getArchiviato());

        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).save(esistente);
    }

    @Test
    void deveLanciareEccezioneQuandoProdottoNonEsiste() {

        Long id = 999L;

        Prodotto datiAggiornati = new Prodotto(
                null,
                "P999",
                "Qualsiasi",
                new BigDecimal("5.00"),
                new BigDecimal("7.00"),
                10,
                2,
                false
        );

        when(repository.findById(id)).thenReturn(Optional.empty());

        // When + Then
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> useCase.eseguire(id, datiAggiornati)
        );

        assertEquals("Prodotto con id " + id + " non trovato", ex.getMessage());

        verify(repository, times(1)).findById(id);
        verify(repository, never()).save(any());
    }
}
