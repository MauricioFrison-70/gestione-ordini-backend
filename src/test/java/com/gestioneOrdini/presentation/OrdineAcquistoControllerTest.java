package com.gestioneOrdini.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestioneOrdini.application.acquisto.dto.FornitoreRiferimentoResponse;
import com.gestioneOrdini.application.acquisto.dto.OrdineAcquistoRequest;
import com.gestioneOrdini.application.acquisto.dto.OrdineAcquistoResponse;
import com.gestioneOrdini.application.acquisto.usecase.AnnullaOrdineAcquistoUseCase;
import com.gestioneOrdini.application.acquisto.usecase.CreateOrdineAcquistoUseCase;
import com.gestioneOrdini.application.acquisto.usecase.DeleteOrdineAcquistoUseCase;
import com.gestioneOrdini.application.acquisto.usecase.GetOrdineAcquistoUseCase;
import com.gestioneOrdini.application.acquisto.usecase.ListOrdiniAcquistoUseCase;
import com.gestioneOrdini.application.acquisto.usecase.RiceviOrdineAcquistoUseCase;
import com.gestioneOrdini.domain.acquisto.model.OrdineAcquisto;
import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import com.gestioneOrdini.infrastructure.persistence.mapper.acquisto.OrdineAcquistoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrdineAcquistoController.class)
class OrdineAcquistoControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private CreateOrdineAcquistoUseCase createUseCase;
    @MockBean private GetOrdineAcquistoUseCase getUseCase;
    @MockBean private ListOrdiniAcquistoUseCase listUseCase;
    @MockBean private DeleteOrdineAcquistoUseCase deleteUseCase;
    @MockBean private RiceviOrdineAcquistoUseCase riceviUseCase;
    @MockBean private AnnullaOrdineAcquistoUseCase annullaUseCase;
    @MockBean private OrdineAcquistoMapper mapper;

    private OrdineAcquisto ordine;
    private OrdineAcquistoResponse response;

    @BeforeEach
    void setUp() {
        Agente fornitore = new Agente(
                2L, "Fornitore", "fornitore@example.com",
                TipoAgente.FORNITORE, false);
        ordine = new OrdineAcquisto(
                1L, "OA-2026-000001", fornitore,
                LocalDateTime.of(2026, 8, 22, 10, 0), null, null);
        response = new OrdineAcquistoResponse(
                1L, "OA-2026-000001",
                new FornitoreRiferimentoResponse(2L, "Fornitore"),
                ordine.getDataRegistrazione(), null, null);
    }

    @Test
    void dovrebbeCreareOrdineDiAcquisto() throws Exception {
        Mockito.when(createUseCase.eseguire(any())).thenReturn(ordine);
        Mockito.when(mapper.toResponse(ordine)).thenReturn(response);

        mockMvc.perform(post("/api/ordini-acquisto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OrdineAcquistoRequest(2L))))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location", "/api/ordini-acquisto/1"))
                .andExpect(jsonPath("$.numeroOrdine")
                        .value("OA-2026-000001"));
    }

    @Test
    void dovrebbeRicevereOrdineDiAcquisto() throws Exception {
        Mockito.when(riceviUseCase.eseguire(1L)).thenReturn(ordine);
        Mockito.when(mapper.toResponse(ordine)).thenReturn(response);

        mockMvc.perform(post("/api/ordini-acquisto/1/ricevere"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
