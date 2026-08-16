package com.gestioneOrdini.application.prodotto.usecase;

import com.gestioneOrdini.domain.prodotto.event.ProdottoCreatoEvent;
import com.gestioneOrdini.domain.prodotto.event.handler.ProdottoCreatoEventHandler;
import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import com.gestioneOrdini.domain.prodotto.repository.ProdottoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test del caso d'uso {@link CreateProdottoUseCase}.
 * <p>
 * Verifica che:
 * <ul>
 *     <li>il prodotto venga salvato correttamente tramite il repository</li>
 *     <li>l'evento di dominio {@link ProdottoCreatoEvent} venga generato</li>
 *     <li>l'handler dell'evento venga invocato con i dati corretti</li>
 * </ul>
 */
class CreateProdottoUseCaseTest {

    private ProdottoRepository repository;
    private ProdottoCreatoEventHandler eventHandler;
    private CreateProdottoUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(ProdottoRepository.class);
        eventHandler = mock(ProdottoCreatoEventHandler.class);
        useCase = new CreateProdottoUseCase(repository, eventHandler);
    }

    @Test
    void deveCreareProdottoEGenerareEvento() {

        // Given: costruzione del prodotto con tipi reali
        Prodotto prodotto = new Prodotto(
                1L,
                "P001",
                "Prodotto di test",
                new BigDecimal("10.50"),
                new BigDecimal("15.90"),
                100,
                10,
                false
        );

        when(repository.save(prodotto)).thenReturn(prodotto);

        // When
        Prodotto risultato = useCase.eseguire(prodotto);

        // Then: verifica dei dati salvati
        assertNotNull(risultato);
        assertEquals(1L, risultato.getId());
        assertEquals("P001", risultato.getCodice());
        assertEquals("Prodotto di test", risultato.getDescrizione());
        assertEquals(new BigDecimal("10.50"), risultato.getValoreAcquisto());
        assertEquals(new BigDecimal("15.90"), risultato.getValoreVendita());
        assertEquals(100, risultato.getQuantita());
        assertEquals(10, risultato.getScortaMinima());
        assertFalse(risultato.getArchiviato());

        verify(repository, times(1)).save(prodotto);

        // Cattura dell'evento generato
        ArgumentCaptor<ProdottoCreatoEvent> eventCaptor = ArgumentCaptor.forClass(ProdottoCreatoEvent.class);
        verify(eventHandler, times(1)).handle(eventCaptor.capture());

        ProdottoCreatoEvent evento = eventCaptor.getValue();

        assertEquals(1L, evento.getIdProdotto());
        assertEquals("P001", evento.getCodice());
        assertEquals("Prodotto di test", evento.getDescrizione());
        assertEquals(new BigDecimal("10.50"), evento.getValoreAcquisto());
        assertEquals(new BigDecimal("15.90"), evento.getValoreVendita());
        assertEquals(100, evento.getQuantita());
        assertEquals(10, evento.getScortaMinima());
    }

    @Test
    void devePropagareErroreENonGenerareEventoQuandoIlSalvataggioFallisce() {
        Prodotto prodotto = new Prodotto(
                "P001",
                "Prodotto di test",
                new BigDecimal("10.50"),
                new BigDecimal("15.90"),
                100,
                10,
                false
        );
        RuntimeException errore = new RuntimeException("Errore di persistenza");

        when(repository.save(prodotto)).thenThrow(errore);

        RuntimeException eccezione = assertThrows(
                RuntimeException.class,
                () -> useCase.eseguire(prodotto)
        );

        assertSame(errore, eccezione);
        verify(repository).save(prodotto);
        verifyNoInteractions(eventHandler);
    }
}
