package com.gestioneOrdini.application.agente.usecase;

import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import com.gestioneOrdini.domain.agente.repository.AgenteRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test del caso d'uso {@link UpdateAgenteUseCase}.
 * <p>
 * Verifica che:
 * <ul>
 *     <li>l'agente venga aggiornato correttamente quando esiste</li>
 *     <li>venga sollevata un'eccezione quando l'agente non esiste</li>
 * </ul>
 */
class UpdateAgenteUseCaseTest {

    private AgenteRepository repository;
    private UpdateAgenteUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(AgenteRepository.class);
        useCase = new UpdateAgenteUseCase(repository);
    }

    @Test
    void deveAggiornareAgenteQuandoEsiste() {

        Long id = 1L;

        // Agente esistente nel repository
        Agente esistente = new Agente(
                id,
                "Mauricio",
                "mauricio@email.com",
                TipoAgente.CLIENTE,
                false
        );

        // Nuovi dati aggiornati
        Agente aggiornato = new Agente(
                null,
                "Giovanni",
                "giovanni@email.com",
                TipoAgente.FORNITORE,
                true
        );

        when(repository.findById(id)).thenReturn(Optional.of(esistente));
        when(repository.save(esistente)).thenReturn(esistente);

        // When
        Agente risultato = useCase.eseguire(id, aggiornato);

        // Then: verifica aggiornamento dei campi
        assertNotNull(risultato);
        assertEquals("Giovanni", risultato.getNome());
        assertEquals("giovanni@email.com", risultato.getEmail());
        assertEquals(TipoAgente.FORNITORE, risultato.getTipoAgente());
        assertTrue(risultato.getArchiviato());

        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).save(esistente);
    }

    @Test
    void deveLanciareEccezioneQuandoAgenteNonEsiste() {

        Long id = 999L;

        Agente datiAggiornati = new Agente(
                null,
                "Nome",
                "email@email.com",
                TipoAgente.CLIENTE,
                false
        );

        when(repository.findById(id)).thenReturn(Optional.empty());

        // When + Then
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> useCase.eseguire(id, datiAggiornati)
        );

        assertEquals("Agente con id " + id + " non trovato", ex.getMessage());

        verify(repository, times(1)).findById(id);
        verify(repository, never()).save(any());
    }
}
