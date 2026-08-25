package com.gestioneOrdini.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestioneOrdini.application.ordine.dto.*;
import com.gestioneOrdini.application.ordine.usecase.*;
import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import com.gestioneOrdini.domain.ordine.exception.OrdineVenditaAnnullatoException;
import com.gestioneOrdini.domain.ordine.model.OrdineVendita;
import com.gestioneOrdini.domain.ordine.exception.OrdineVenditaRilasciatoException;
import com.gestioneOrdini.domain.ordine.exception.ScortaInsufficienteException;
import com.gestioneOrdini.infrastructure.persistence.mapper.ordine.OrdineVenditaMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrdineVenditaController.class)
class OrdineVenditaControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private CreateOrdineVenditaUseCase createUseCase;
    @MockBean private GetOrdineVenditaUseCase getUseCase;
    @MockBean private ListOrdiniVenditaUseCase listUseCase;
    @MockBean private UpdateOrdineVenditaUseCase updateUseCase;
    @MockBean private DeleteOrdineVenditaUseCase deleteUseCase;
    @MockBean private RilasciaOrdineVenditaUseCase rilasciaUseCase;
    @MockBean private AnnullaOrdineVenditaUseCase annullaUseCase;
    @MockBean private OrdineVenditaMapper mapper;

    private OrdineVendita ordine;
    private OrdineVenditaResponse response;
    private OrdineVenditaRequest request;

    @BeforeEach
    void setUp() {
        Agente cliente = agente(1L, TipoAgente.CLIENTE);
        Agente venditore = agente(2L, TipoAgente.VENDITORE);
        Agente trasportatore = agente(3L, TipoAgente.TRASPORTATORE);
        ordine = new OrdineVendita(10L, "OV-2026-000010", cliente, venditore,
                trasportatore, LocalDateTime.of(2026, 8, 21, 10, 0), null);
        request = new OrdineVenditaRequest(1L, 2L, 3L);
        response = new OrdineVenditaResponse(10L, "OV-2026-000010",
                new AgenteRiferimentoResponse(1L, "CLIENTE"),
                new AgenteRiferimentoResponse(2L, "VENDITORE"),
                new AgenteRiferimentoResponse(3L, "TRASPORTATORE"),
                ordine.getDataRegistrazione(), null, null);
    }

    @Test
    void dovrebbeCreareOrdineViaApi() throws Exception {
        Mockito.when(createUseCase.eseguire(any())).thenReturn(ordine);
        Mockito.when(mapper.toResponse(ordine)).thenReturn(response);

        mockMvc.perform(post("/api/ordini-vendita").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/ordini-vendita/10"))
                .andExpect(jsonPath("$.numeroOrdine").value("OV-2026-000010"))
                .andExpect(jsonPath("$.cliente.id").value(1))
                .andExpect(jsonPath("$.dataRegistrazione").exists())
                .andExpect(jsonPath("$.dataRilascio").doesNotExist());
    }

    @Test
    void dovrebbeCercareEdElencareOrdini() throws Exception {
        Mockito.when(getUseCase.eseguire(10L)).thenReturn(ordine);
        Mockito.when(listUseCase.eseguire()).thenReturn(List.of(ordine));
        Mockito.when(mapper.toResponse(ordine)).thenReturn(response);

        mockMvc.perform(get("/api/ordini-vendita/10"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10));
        mockMvc.perform(get("/api/ordini-vendita"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    void dovrebbeAggiornareOrdinePendente() throws Exception {
        Mockito.when(updateUseCase.eseguire(eq(10L), any())).thenReturn(ordine);
        Mockito.when(mapper.toResponse(ordine)).thenReturn(response);

        mockMvc.perform(put("/api/ordini-vendita/10").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void dovrebbeRilasciareOrdineViaApi() throws Exception {
        Mockito.when(rilasciaUseCase.eseguire(10L)).thenReturn(ordine);
        Mockito.when(mapper.toResponse(ordine)).thenReturn(response);

        mockMvc.perform(post("/api/ordini-vendita/10/rilasciare"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void dovrebbeRifiutareRilascioQuandoLaScortaNonESufficiente() throws Exception {
        Mockito.when(rilasciaUseCase.eseguire(10L))
                .thenThrow(new ScortaInsufficienteException(
                        "Prodotti: P001 (disponibile: 1, richiesta: 3)."));

        mockMvc.perform(post("/api/ordini-vendita/10/rilasciare"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codice").value("SCORTA_INSUFFICIENTE"))
                .andExpect(jsonPath("$.errore").value(
                        "Scorta insufficiente. L'operazione è stata annullata. "
                                + "Prodotti: P001 (disponibile: 1, richiesta: 3)."));
    }

    @Test
    void dovrebbeAnnullareOrdineViaApi() throws Exception {
        Mockito.when(annullaUseCase.eseguire(10L)).thenReturn(ordine);
        Mockito.when(mapper.toResponse(ordine)).thenReturn(response);

        mockMvc.perform(post("/api/ordini-vendita/10/annullare"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void dovrebbeRifiutareRequestSenzaAgenti() throws Exception {
        mockMvc.perform(post("/api/ordini-vendita").contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.clienteId").exists())
                .andExpect(jsonPath("$.venditoreId").exists())
                .andExpect(jsonPath("$.trasportatoreId").exists());
    }

    @Test
    void dovrebbeRitornareBadRequestPerTipoAgenteErrato() throws Exception {
        Mockito.when(createUseCase.eseguire(any()))
                .thenThrow(new IllegalArgumentException("Il cliente deve essere di tipo CLIENTE"));
        mockMvc.perform(post("/api/ordini-vendita").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errore").value("Il cliente deve essere di tipo CLIENTE"));
    }

    @Test
    void dovrebbeEliminareOrdineSenzaDataRilascio() throws Exception {
        mockMvc.perform(delete("/api/ordini-vendita/10"))
                .andExpect(status().isNoContent());
        Mockito.verify(deleteUseCase).eseguire(10L);
    }

    @Test
    void dovrebbeRifiutareEliminazioneOrdineRilasciato() throws Exception {
        Mockito.doThrow(new OrdineVenditaRilasciatoException("OV-2026-000010"))
                .when(deleteUseCase).eseguire(10L);

        mockMvc.perform(delete("/api/ordini-vendita/10"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codice").value("ORDINE_VENDITA_RILASCIATO"))
                .andExpect(jsonPath("$.errore").value(
                        "L'ordine di vendita OV-2026-000010 non può essere eliminato "
                                + "perché ha già una data di rilascio."));
    }

    @Test
    void dovrebbeRifiutareEliminazioneOrdineAnnullato() throws Exception {
        Mockito.doThrow(new OrdineVenditaAnnullatoException("OV-2026-000010"))
                .when(deleteUseCase).eseguire(10L);

        mockMvc.perform(delete("/api/ordini-vendita/10"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codice").value("ORDINE_VENDITA_ANNULLATO"))
                .andExpect(jsonPath("$.errore").value(
                        "L'ordine di vendita OV-2026-000010 non può essere eliminato "
                                + "perché ha già una data di annullamento."));
    }

    private Agente agente(Long id, TipoAgente tipo) {
        return new Agente(id, tipo.name(), tipo.name().toLowerCase() + "@example.com", tipo, false);
    }
}
