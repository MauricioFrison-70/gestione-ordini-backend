package com.gestioneOrdini.application.prodotto.usecase;

import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import com.gestioneOrdini.domain.prodotto.repository.ProdottoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test del caso d'uso {@link ListProdottiUseCase}.
 * <p>
 * Verifica che:
 * <ul>
 *     <li>venga restituita la lista completa dei prodotti</li>
 *     <li>la lista sia immutabile</li>
 *     <li>venga gestito correttamente anche il caso di lista vuota</li>
 * </ul>
 */
class ListProdottiUseCaseTest {

    private ProdottoRepository repository;
    private ListProdottiUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(ProdottoRepository.class);
        useCase = new ListProdottiUseCase(repository);
    }

    @Test
    void deveRestituireListaProdottiQuandoEsistono() {

        // Given: due prodotti presenti nel repository
        Prodotto p1 = new Prodotto(
                1L,
                "P001",
                "Prodotto di test 1",
                new BigDecimal("10.50"),
                new BigDecimal("15.90"),
                100,
                10,
                false
        );

        Prodotto p2 = new Prodotto(
                2L,
                "P002",
                "Prodotto di test 2",
                new BigDecimal("5.00"),
                new BigDecimal("8.50"),
                50,
                5,
                false
        );

        when(repository.findAll()).thenReturn(List.of(p1, p2));

        // When
        List<Prodotto> risultato = useCase.eseguire();

        // Then
        assertNotNull(risultato);
        assertEquals(2, risultato.size());
        assertEquals("P001", risultato.get(0).getCodice());
        assertEquals("P002", risultato.get(1).getCodice());

        verify(repository, times(1)).findAll();

        // Verifica che la lista sia immutabile
        assertThrows(UnsupportedOperationException.class, () -> risultato.add(p1));
    }

    @Test
    void deveRestituireListaVuotaQuandoNonEsistonoProdotti() {

        // Given: repository restituisce lista vuota
        when(repository.findAll()).thenReturn(List.of());

        // When
        List<Prodotto> risultato = useCase.eseguire();

        // Then
        assertNotNull(risultato);
        assertTrue(risultato.isEmpty());

        verify(repository, times(1)).findAll();

        // Lista deve essere immutabile anche se vuota
        assertThrows(UnsupportedOperationException.class, () -> risultato.add(null));
    }
}
