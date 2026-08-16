package com.gestioneOrdini.application.agente.usecase;

import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import com.gestioneOrdini.domain.agente.repository.AgenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test del caso d'uso {@link ListAgentiUseCase}.
 * <p>
 * Verifica che:
 * <ul>
 *     <li>venga restituita la lista completa degli agenti</li>
 *     <li>la lista sia immutabile</li>
 *     <li>venga gestito correttamente anche il caso di lista vuota</li>
 * </ul>
 */
class ListAgentiUseCaseTest {

    private AgenteRepository repository;
    private ListAgentiUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(AgenteRepository.class);
        useCase = new ListAgentiUseCase(repository);
    }

    @Test
    void deveRestituireListaAgentiQuandoEsistono() {

        // Given: due agenti presenti nel repository
        Agente a1 = new Agente(
                1L,
                "Mauricio",
                "mauricio@email.com",
                TipoAgente.CLIENTE,
                false
        );

        Agente a2 = new Agente(
                2L,
                "Giovanni",
                "giovanni@email.com",
                TipoAgente.FORNITORE,
                false
        );

        when(repository.findAll()).thenReturn(List.of(a1, a2));

        // When
        List<Agente> risultato = useCase.eseguire();

        // Then
        assertNotNull(risultato);
        assertEquals(2, risultato.size());
        assertEquals("Mauricio", risultato.get(0).getNome());
        assertEquals("Giovanni", risultato.get(1).getNome());

        verify(repository, times(1)).findAll();

        // Verifica che la lista sia immutabile
        assertThrows(UnsupportedOperationException.class, () -> risultato.add(a1));
    }

    @Test
    void deveRestituireListaVuotaQuandoNonEsistonoAgenti() {

        // Given: repository restituisce lista vuota
        when(repository.findAll()).thenReturn(List.of());

        // When
        List<Agente> risultato = useCase.eseguire();

        // Then
        assertNotNull(risultato);
        assertTrue(risultato.isEmpty());

        verify(repository, times(1)).findAll();

        // Lista deve essere immutabile anche se vuota
        assertThrows(UnsupportedOperationException.class, () -> risultato.add(null));
    }
}
