package com.gestioneOrdini.infrastructure.persistence.mapper.prodotto;

import com.gestioneOrdini.application.prodotto.dto.ProdottoRequest;
import com.gestioneOrdini.application.prodotto.dto.ProdottoResponse;
import com.gestioneOrdini.application.prodotto.dto.ProdottoUpdateRequest;
import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import com.gestioneOrdini.infrastructure.persistence.entity.ProdottoEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProdottoMapperTest {

    private final ProdottoMapper mapper = new ProdottoMapper();

    @Test
    void deveMappareRequestADominio() {

        ProdottoRequest request = new ProdottoRequest(
                "P001",
                "Notebook Dell",
                new BigDecimal("1500.00"),
                new BigDecimal("2200.00"),
                2,
                false
        );

        Prodotto domain = mapper.toDomain(request);

        assertNull(domain.getId());
        assertEquals("P001", domain.getCodice());
        assertEquals("Notebook Dell", domain.getDescrizione());
        assertEquals(new BigDecimal("1500.00"), domain.getValoreAcquisto());
        assertEquals(new BigDecimal("2200.00"), domain.getValoreVendita());
        assertEquals(0, domain.getQuantita());
        assertEquals(2, domain.getScortaMinima());
        assertFalse(domain.getArchiviato());
    }

    @Test
    void deveMappareDominioAResponse() {

        LocalDateTime dataRegistrazione = LocalDateTime.of(2026, 8, 13, 10, 30);
        Prodotto domain = new Prodotto(
                1L,
                "P001",
                "Notebook Dell",
                new BigDecimal("1500.00"),
                new BigDecimal("2200.00"),
                10,
                2,
                false,
                dataRegistrazione
        );

        ProdottoResponse response = mapper.toResponse(domain);

        assertEquals(1L, response.id());
        assertEquals("P001", response.codice());
        assertEquals("Notebook Dell", response.descrizione());
        assertEquals(new BigDecimal("1500.00"), response.valoreAcquisto());
        assertEquals(new BigDecimal("2200.00"), response.valoreVendita());
        assertEquals(10, response.quantita());
        assertEquals(2, response.scortaMinima());
        assertFalse(response.archiviato());
        assertEquals(dataRegistrazione, response.dataRegistrazione());
    }

    @Test
    void deveMappareEntityADominio() {

        LocalDateTime dataRegistrazione = LocalDateTime.of(2026, 8, 13, 10, 30);
        ProdottoEntity entity = new ProdottoEntity(
                1L,
                "P001",
                "Notebook Dell",
                new BigDecimal("1500.00"),
                new BigDecimal("2200.00"),
                10,
                2,
                false,
                dataRegistrazione
        );

        Prodotto domain = mapper.toDomain(entity);

        assertEquals(1L, domain.getId());
        assertEquals("P001", domain.getCodice());
        assertEquals("Notebook Dell", domain.getDescrizione());
        assertEquals(new BigDecimal("1500.00"), domain.getValoreAcquisto());
        assertEquals(new BigDecimal("2200.00"), domain.getValoreVendita());
        assertEquals(10, domain.getQuantita());
        assertEquals(2, domain.getScortaMinima());
        assertFalse(domain.getArchiviato());
        assertEquals(dataRegistrazione, domain.getDataRegistrazione());
    }

    @Test
    void deveMappareDominioAEntity() {
        LocalDateTime dataRegistrazione = LocalDateTime.of(2026, 8, 13, 10, 30);
        Prodotto domain = new Prodotto(
                1L,
                "P001",
                "Notebook Dell",
                new BigDecimal("1500.00"),
                new BigDecimal("2200.00"),
                10,
                2,
                false,
                dataRegistrazione
        );

        ProdottoEntity entity = mapper.toEntity(domain);

        assertEquals(1L, entity.getId());
        assertEquals("P001", entity.getCodice());
        assertEquals("Notebook Dell", entity.getDescrizione());
        assertEquals(new BigDecimal("1500.00"), entity.getValoreAcquisto());
        assertEquals(new BigDecimal("2200.00"), entity.getValoreVendita());
        assertEquals(10, entity.getQuantita());
        assertEquals(2, entity.getScortaMinima());
        assertFalse(entity.getArchiviato());
        assertEquals(dataRegistrazione, entity.getDataRegistrazione());
    }

    @Test
    void deveMappareRequestDiAggiornamentoPreservandoCodiceEGiacenza() {
        ProdottoUpdateRequest request = new ProdottoUpdateRequest(
                "Notebook Dell aggiornato",
                new BigDecimal("1600.00"),
                new BigDecimal("2300.00"),
                3,
                true
        );
        Prodotto esistente = new Prodotto(
                1L,
                "P001",
                "Notebook Dell",
                new BigDecimal("1500.00"),
                new BigDecimal("2200.00"),
                10,
                2,
                false
        );

        Prodotto domain = mapper.toDomain(request, esistente);

        assertNull(domain.getId());
        assertEquals("P001", domain.getCodice());
        assertEquals("Notebook Dell aggiornato", domain.getDescrizione());
        assertEquals(new BigDecimal("1600.00"), domain.getValoreAcquisto());
        assertEquals(new BigDecimal("2300.00"), domain.getValoreVendita());
        assertEquals(10, domain.getQuantita());
        assertEquals(3, domain.getScortaMinima());
        assertTrue(domain.getArchiviato());
    }
}
