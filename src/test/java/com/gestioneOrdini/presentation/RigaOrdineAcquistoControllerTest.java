package com.gestioneOrdini.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestioneOrdini.application.acquisto.dto.RigaOrdineAcquistoRequest;
import com.gestioneOrdini.application.acquisto.dto.RigaOrdineAcquistoResponse;
import com.gestioneOrdini.application.acquisto.usecase.CreateRigaOrdineAcquistoUseCase;
import com.gestioneOrdini.application.acquisto.usecase.DeleteRigaOrdineAcquistoUseCase;
import com.gestioneOrdini.application.acquisto.usecase.ListRigheOrdineAcquistoUseCase;
import com.gestioneOrdini.application.acquisto.usecase.UpdateRigaOrdineAcquistoUseCase;
import com.gestioneOrdini.domain.acquisto.model.RigaOrdineAcquisto;
import com.gestioneOrdini.infrastructure.persistence.mapper.acquisto.RigaOrdineAcquistoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RigaOrdineAcquistoController.class)
class RigaOrdineAcquistoControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private CreateRigaOrdineAcquistoUseCase createUseCase;
    @MockBean private UpdateRigaOrdineAcquistoUseCase updateUseCase;
    @MockBean private ListRigheOrdineAcquistoUseCase listUseCase;
    @MockBean private DeleteRigaOrdineAcquistoUseCase deleteUseCase;
    @MockBean private RigaOrdineAcquistoMapper mapper;

    @Test
    void dovrebbeAggiornareUnaRiga() throws Exception {
        RigaOrdineAcquisto riga = org.mockito.Mockito
                .mock(RigaOrdineAcquisto.class);
        RigaOrdineAcquistoResponse response =
                new RigaOrdineAcquistoResponse(
                        30L,
                        10L,
                        "P001",
                        "Prodotto",
                        4,
                        new BigDecimal("3.75"),
                        new BigDecimal("15.00"));
        when(updateUseCase.eseguire(
                org.mockito.ArgumentMatchers.eq(10L),
                org.mockito.ArgumentMatchers.eq(30L),
                any(RigaOrdineAcquistoRequest.class)))
                .thenReturn(riga);
        when(mapper.toResponse(riga)).thenReturn(response);

        mockMvc.perform(put("/api/ordini-acquisto/10/righe/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RigaOrdineAcquistoRequest(
                                        "P001",
                                        4,
                                        new BigDecimal("3.75")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(30))
                .andExpect(jsonPath("$.quantita").value(4))
                .andExpect(jsonPath("$.valoreUnitario").value(3.75));
    }
}
