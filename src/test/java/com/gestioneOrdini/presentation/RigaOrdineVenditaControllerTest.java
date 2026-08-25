package com.gestioneOrdini.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestioneOrdini.application.ordine.dto.RigaOrdineVenditaRequest;
import com.gestioneOrdini.application.ordine.dto.RigaOrdineVenditaResponse;
import com.gestioneOrdini.application.ordine.usecase.CreateRigaOrdineVenditaUseCase;
import com.gestioneOrdini.application.ordine.usecase.DeleteRigaOrdineVenditaUseCase;
import com.gestioneOrdini.application.ordine.usecase.GetRigaOrdineVenditaUseCase;
import com.gestioneOrdini.application.ordine.usecase.ListRigheOrdineVenditaUseCase;
import com.gestioneOrdini.application.ordine.usecase.UpdateRigaOrdineVenditaUseCase;
import com.gestioneOrdini.domain.ordine.exception.OrdineVenditaNonModificabileException;
import com.gestioneOrdini.domain.ordine.exception.ProdottoGiaPresenteNellOrdineException;
import com.gestioneOrdini.domain.ordine.model.RigaOrdineVendita;
import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import com.gestioneOrdini.infrastructure.persistence.mapper.ordine.RigaOrdineVenditaMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RigaOrdineVenditaController.class)
class RigaOrdineVenditaControllerTest {
    private static final String URL = "/api/ordini-vendita/10/righe";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private CreateRigaOrdineVenditaUseCase createUseCase;
    @MockBean private UpdateRigaOrdineVenditaUseCase updateUseCase;
    @MockBean private GetRigaOrdineVenditaUseCase getUseCase;
    @MockBean private ListRigheOrdineVenditaUseCase listUseCase;
    @MockBean private DeleteRigaOrdineVenditaUseCase deleteUseCase;
    @MockBean private RigaOrdineVenditaMapper mapper;

    private RigaOrdineVenditaRequest request;
    private RigaOrdineVendita riga;
    private RigaOrdineVenditaResponse response;

    @BeforeEach
    void setUp() {
        request = new RigaOrdineVenditaRequest(
                "P001", 2, new BigDecimal("10.20"));
        Prodotto prodotto = new Prodotto(20L, "P001", "Prodotto test",
                new BigDecimal("5.00"), new BigDecimal("10.20"),
                10, 1, false);
        riga = new RigaOrdineVendita(
                30L, 10L, prodotto, request.quantita(), request.valoreUnitario());
        response = new RigaOrdineVenditaResponse(
                30L, 10L, "P001", "Prodotto test",
                request.quantita(), request.valoreUnitario(), new BigDecimal("20.40"));
    }

    @Test
    void dovrebbeCreareRiga() throws Exception {
        Mockito.when(createUseCase.eseguire(eq(10L), any())).thenReturn(riga);
        Mockito.when(mapper.toResponse(riga)).thenReturn(response);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", URL + "/30"))
                .andExpect(jsonPath("$.codiceProdotto").value("P001"))
                .andExpect(jsonPath("$.quantita").value(2))
                .andExpect(jsonPath("$.valoreUnitario").value(10.20))
                .andExpect(jsonPath("$.totaleRiga").value(20.40));
    }

    @Test
    void dovrebbeCercareEdElencareRighe() throws Exception {
        Mockito.when(getUseCase.eseguire(10L, 30L)).thenReturn(riga);
        Mockito.when(listUseCase.eseguire(10L)).thenReturn(List.of(riga));
        Mockito.when(mapper.toResponse(riga)).thenReturn(response);

        mockMvc.perform(get(URL + "/30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(30));
        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(30));
    }

    @Test
    void dovrebbeAggiornareRiga() throws Exception {
        Mockito.when(updateUseCase.eseguire(eq(10L), eq(30L), any())).thenReturn(riga);
        Mockito.when(mapper.toResponse(riga)).thenReturn(response);

        mockMvc.perform(put(URL + "/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codiceProdotto").value("P001"));
    }

    @Test
    void dovrebbeEliminareRiga() throws Exception {
        mockMvc.perform(delete(URL + "/30"))
                .andExpect(status().isNoContent());
        Mockito.verify(deleteUseCase).eseguire(10L, 30L);
    }

    @Test
    void dovrebbeRifiutareCampiInvalidi() throws Exception {
        var invalida = new RigaOrdineVenditaRequest(
                "", 0, new BigDecimal("-0.01"));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codiceProdotto").exists())
                .andExpect(jsonPath("$.quantita").exists())
                .andExpect(jsonPath("$.valoreUnitario").exists());
    }

    @Test
    void dovrebbeRifiutareQuantitaDecimale() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codiceProdotto": "P001",
                                  "quantita": 2.5,
                                  "valoreUnitario": 10.20
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errore")
                        .value("I dati della richiesta non hanno un formato valido"));
    }

    @Test
    void dovrebbeRitornareConflittoPerProdottoDuplicato() throws Exception {
        Mockito.when(createUseCase.eseguire(eq(10L), any()))
                .thenThrow(new ProdottoGiaPresenteNellOrdineException("P001", 10L));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codice")
                        .value("PRODOTTO_GIA_PRESENTE_NELL_ORDINE"));
    }

    @Test
    void dovrebbeRitornareConflittoPerOrdineNonModificabile() throws Exception {
        Mockito.when(updateUseCase.eseguire(eq(10L), eq(30L), any()))
                .thenThrow(new OrdineVenditaNonModificabileException(
                        "OV-2026-000010", "è già stato rilasciato"));

        mockMvc.perform(put(URL + "/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codice")
                        .value("ORDINE_VENDITA_NON_MODIFICABILE"));
    }
}
