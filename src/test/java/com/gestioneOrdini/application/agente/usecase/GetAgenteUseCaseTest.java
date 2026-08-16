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
 * Test del caso d'uso {@link GetAgenteUseCase}.
 * <p>
 * Verifica che:
 * <ul>
 *     <li>l'agente venga correttamente recuperato quando esiste</li>
 *     <li>venga sollevata un'eccezione quando l'agente non esiste</li>
 * </ul>
 */
class GetAgenteUseCaseTest {

    private AgenteRepository repository;
    private GetAgenteUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(AgenteRepository.class);
        useCase = new GetAgenteUseCase(repository);
    }

    @Test
    void deveRestituireAgenteQuandoEsiste() {

        Long id = 1L;

        // Given: agente presente nel repository
        Agente agente = new Agente(
                id,
                "Mauricio",
                "mauricio@email.com",
                TipoAgente.CLIENTE,
                false
        );

        when(repository.findById(id)).thenReturn(Optional.of(agente));

        // When
        Agente risultato = useCase.eseguire(id);

        // Then
        assertNotNull(risultato);
        assertEquals(id, risultato.getId());
        assertEquals("Mauricio", risultato.getNome());
        assertEquals("mauricio@email.com", risultato.getEmail());
        assertEquals(TipoAgente.CLIENTE, risultato.getTipoAgente());
        assertFalse(risultato.getArchiviato());

        verify(repository, times(1)).findById(id);
    }

    @Test
    void deveLanciareEccezioneQuandoAgenteNonEsiste() {

        Long id = 999L;

        // Given: repository non trova l'agente
        when(repository.findById(id)).thenReturn(Optional.empty());

        // When + Then
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> useCase.eseguire(id)
        );

        assertEquals("Agente con id " + id + " non trovato", ex.getMessage());

        verify(repository, times(1)).findById(id);
    }
}
