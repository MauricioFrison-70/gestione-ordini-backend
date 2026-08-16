package com.gestioneOrdini.infrastructure.persistence.mapper.agente;

import com.gestioneOrdini.application.agente.dto.AgenteRequest;
import com.gestioneOrdini.application.agente.dto.AgenteResponse;
import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import com.gestioneOrdini.infrastructure.persistence.entity.AgenteEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AgenteMapperTest {

    private final AgenteMapper mapper = new AgenteMapper();

    @Test
    void deveMappareRequestADominio() {

        AgenteRequest request = new AgenteRequest(
                "Mauricio",
                "mauricio@email.com",
                TipoAgente.CLIENTE,
                false
        );

        Agente domain = mapper.toDomain(request);

        assertNull(domain.getId());
        assertEquals("Mauricio", domain.getNome());
        assertEquals("mauricio@email.com", domain.getEmail());
        assertEquals(TipoAgente.CLIENTE, domain.getTipoAgente());
        assertFalse(domain.getArchiviato());
    }

    @Test
    void deveMappareDominioAResponse() {

        LocalDateTime dataRegistrazione = LocalDateTime.of(2026, 8, 13, 10, 30);
        Agente domain = new Agente(
                1L,
                "Mauricio",
                "mauricio@email.com",
                TipoAgente.CLIENTE,
                false,
                dataRegistrazione
        );

        AgenteResponse response = mapper.toResponse(domain);

        assertEquals(1L, response.id());
        assertEquals("Mauricio", response.nome());
        assertEquals("mauricio@email.com", response.email());
        assertEquals(TipoAgente.CLIENTE, response.tipoAgente());
        assertFalse(response.archiviato());
        assertEquals(dataRegistrazione, response.dataRegistrazione());
    }

    @Test
    void deveMappareEntityADominio() {

        LocalDateTime dataRegistrazione = LocalDateTime.of(2026, 8, 13, 10, 30);
        AgenteEntity entity = new AgenteEntity(
                1L,
                "Mauricio",
                "mauricio@email.com",
                TipoAgente.CLIENTE,
                false,
                dataRegistrazione
        );

        Agente domain = mapper.toDomain(entity);
        
        assertEquals(1L, domain.getId());
        assertEquals("Mauricio", domain.getNome());
        assertEquals("mauricio@email.com", domain.getEmail());
        assertEquals(TipoAgente.CLIENTE, domain.getTipoAgente());
        assertFalse(domain.getArchiviato());
        assertEquals(dataRegistrazione, domain.getDataRegistrazione());
    }

    @Test
    void deveMappareDominioAEntity() {
        LocalDateTime dataRegistrazione = LocalDateTime.of(2026, 8, 13, 10, 30);
        Agente domain = new Agente(
                1L,
                "Mauricio",
                "mauricio@email.com",
                TipoAgente.CLIENTE,
                false,
                dataRegistrazione
        );

        AgenteEntity entity = mapper.toEntity(domain);

        assertEquals(1L, entity.getId());
        assertEquals("Mauricio", entity.getNome());
        assertEquals("mauricio@email.com", entity.getEmail());
        assertEquals(TipoAgente.CLIENTE, entity.getTipoAgente());
        assertFalse(entity.getArchiviato());
        assertEquals(dataRegistrazione, entity.getDataRegistrazione());
    }
}
