package com.gestioneOrdini.application.agente.usecase;

import com.gestioneOrdini.domain.agente.event.AgenteCreatoEvent;
import com.gestioneOrdini.domain.agente.event.handler.AgenteCreatoEventHandler;
import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import com.gestioneOrdini.domain.agente.repository.AgenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test del caso d'uso {@link CreateAgenteUseCase}.
 * <p>
 * Verifica che:
 * <ul>
 *     <li>l'agente venga salvato correttamente tramite il repository</li>
 *     <li>l'evento di dominio {@link AgenteCreatoEvent} venga generato</li>
 *     <li>l'handler dell'evento venga invocato con i dati corretti</li>
 * </ul>
 */
class CreateAgenteUseCaseTest {

    private AgenteRepository repository;
    private AgenteCreatoEventHandler eventHandler;
    private CreateAgenteUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(AgenteRepository.class);
        eventHandler = mock(AgenteCreatoEventHandler.class);
        useCase = new CreateAgenteUseCase(repository, eventHandler);
    }

    @Test
    void deveCreareAgenteEGenerareEvento() {

        // Given: agente costruito con il costruttore reale
        Agente agente = new Agente(
                1L,
                "Mauricio",
                "mauricio@email.com",
                TipoAgente.CLIENTE,
                false
        );

        when(repository.save(agente)).thenReturn(agente);

        // When
        Agente risultato = useCase.eseguire(agente);

        // Then: verifica dei dati salvati
        assertNotNull(risultato);
        assertEquals(1L, risultato.getId());
        assertEquals("Mauricio", risultato.getNome());
        assertEquals("mauricio@email.com", risultato.getEmail());
        assertEquals(TipoAgente.CLIENTE, risultato.getTipoAgente());
        assertFalse(risultato.getArchiviato());

        verify(repository, times(1)).save(agente);

        // Cattura dell'evento generato
        ArgumentCaptor<AgenteCreatoEvent> eventCaptor = ArgumentCaptor.forClass(AgenteCreatoEvent.class);
        verify(eventHandler, times(1)).handle(eventCaptor.capture());

        AgenteCreatoEvent evento = eventCaptor.getValue();

        assertEquals(1L, evento.getIdAgente());
        assertEquals("Mauricio", evento.getNome());
        assertEquals("mauricio@email.com", evento.getEmail());
        assertEquals(TipoAgente.CLIENTE, evento.getTipoAgente());
    }

    @Test
    void devePropagareErroreENonGenerareEventoQuandoIlSalvataggioFallisce() {
        Agente agente = new Agente(
                "Mauricio",
                "mauricio@email.com",
                TipoAgente.CLIENTE,
                false
        );
        RuntimeException errore = new RuntimeException("Errore di persistenza");

        when(repository.save(agente)).thenThrow(errore);

        RuntimeException eccezione = assertThrows(
                RuntimeException.class,
                () -> useCase.eseguire(agente)
        );

        assertSame(errore, eccezione);
        verify(repository).save(agente);
        verifyNoInteractions(eventHandler);
    }
}
