package com.gestioneOrdini.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestioneOrdini.application.agente.dto.AgenteRequest;
import com.gestioneOrdini.application.agente.dto.AgenteResponse;
import com.gestioneOrdini.application.agente.usecase.*;
import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import com.gestioneOrdini.domain.agente.exception.AgenteUtilizzatoException;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import com.gestioneOrdini.infrastructure.persistence.mapper.agente.AgenteMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AgenteController.class)
class AgenteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapperJson;

    @MockBean
    private CreateAgenteUseCase createUseCase;
    @MockBean
    private UpdateAgenteUseCase updateUseCase;
    @MockBean
    private DeleteAgenteUseCase deleteUseCase;
    @MockBean
    private GetAgenteUseCase getUseCase;
    @MockBean
    private ListAgentiUseCase listUseCase;
    @MockBean
    private CheckAgenteUtilizzatoUseCase checkUtilizzatoUseCase;
    @MockBean
    private AgenteMapper mapper;

    private Agente agente;
    private AgenteResponse response;
    private AgenteRequest request;

    @BeforeEach
    void setUp() {

        agente = new Agente(
                1L,
                "Mauricio",
                "mauricio@email.com",
                TipoAgente.CLIENTE,
                false
        );

        response = new AgenteResponse(
                1L,
                "Mauricio",
                "mauricio@email.com",
                TipoAgente.CLIENTE,
                false,
                LocalDateTime.now()
        );

        request = new AgenteRequest(
                "Mauricio",
                "mauricio@email.com",
                TipoAgente.CLIENTE,
                false
        );
    }

    @Test
    void deveCreareAgente() throws Exception {

        Mockito.when(mapper.toDomain(any(AgenteRequest.class))).thenReturn(agente);
        Mockito.when(createUseCase.eseguire(any())).thenReturn(agente);
        Mockito.when(mapper.toResponse(any())).thenReturn(response);

        mockMvc.perform(post("/api/agenti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapperJson.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/agenti/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Mauricio"))
                .andExpect(jsonPath("$.email").value("mauricio@email.com"))
                .andExpect(jsonPath("$.tipoAgente").value("CLIENTE"))
                .andExpect(jsonPath("$.archiviato").value(false))
                .andExpect(jsonPath("$.dataRegistrazione").exists());
    }

    @Test
    void deveRestituireAgentePerId() throws Exception {

        Mockito.when(getUseCase.eseguire(1L)).thenReturn(agente);
        Mockito.when(mapper.toResponse(agente)).thenReturn(response);

        mockMvc.perform(get("/api/agenti/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Mauricio"))
                .andExpect(jsonPath("$.email").value("mauricio@email.com"))
                .andExpect(jsonPath("$.tipoAgente").value("CLIENTE"))
                .andExpect(jsonPath("$.archiviato").value(false))
                .andExpect(jsonPath("$.dataRegistrazione").exists());
    }

    @Test
    void deveElencareAgenti() throws Exception {

        Mockito.when(listUseCase.eseguire()).thenReturn(List.of(agente));
        Mockito.when(mapper.toResponse(any())).thenReturn(response);

        mockMvc.perform(get("/api/agenti"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nome").value("Mauricio"))
                .andExpect(jsonPath("$[0].email").value("mauricio@email.com"))
                .andExpect(jsonPath("$[0].tipoAgente").value("CLIENTE"))
                .andExpect(jsonPath("$[0].archiviato").value(false))
                .andExpect(jsonPath("$[0].dataRegistrazione").exists());
    }

    @Test
    void deveAggiornareAgente() throws Exception {

        Mockito.when(mapper.toDomain(any(AgenteRequest.class))).thenReturn(agente);
        Mockito.when(updateUseCase.eseguire(eq(1L), any())).thenReturn(agente);
        Mockito.when(mapper.toResponse(any())).thenReturn(response);

        mockMvc.perform(put("/api/agenti/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapperJson.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Mauricio"))
                .andExpect(jsonPath("$.email").value("mauricio@email.com"))
                .andExpect(jsonPath("$.tipoAgente").value("CLIENTE"))
                .andExpect(jsonPath("$.archiviato").value(false))
                .andExpect(jsonPath("$.dataRegistrazione").exists());
    }

    @Test
    void deveEliminareAgente() throws Exception {

        Mockito.doNothing().when(deleteUseCase).eseguire(1L);

        mockMvc.perform(delete("/api/agenti/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveRitornareConflittoQuandoAgenteUtilizzatoInOrdine() throws Exception {
        Mockito.doThrow(new AgenteUtilizzatoException()).when(deleteUseCase).eseguire(1L);

        mockMvc.perform(delete("/api/agenti/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codice").value("AGENTE_UTILIZZATO"))
                .andExpect(jsonPath("$.errore").value(
                        "L'agente è utilizzato in uno o più ordini. "
                                + "Vuoi archiviarlo o annullare l'eliminazione?"));
    }

    @Test
    void deveVerificareUtilizzoPrimaDellEliminazione() throws Exception {
        Mockito.when(checkUtilizzatoUseCase.eseguire(1L)).thenReturn(true);

        mockMvc.perform(get("/api/agenti/1/utilizzo-ordini"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.utilizzato").value(true));
    }

    @Test
    void deveRitornareBadRequestQuandoRequestInvalido() throws Exception {

        AgenteRequest invalido = new AgenteRequest(
                "",
                "email-invalida",
                null,
                false
        );

        mockMvc.perform(post("/api/agenti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapperJson.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRitornareNotFoundQuandoAgenteNonEsiste() throws Exception {

        Mockito.when(getUseCase.eseguire(999L))
                .thenThrow(new EntityNotFoundException("Agente", 999L));

        mockMvc.perform(get("/api/agenti/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errore").value("Agente con id 999 non trovato"));
    }

    @Test
    void deveRitornareErroreGenericoSenzaEsporreDettagliInterni() throws Exception {

        Mockito.when(getUseCase.eseguire(1L))
                .thenThrow(new IllegalStateException("dettaglio tecnico riservato"));

        mockMvc.perform(get("/api/agenti/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errore").value("Errore interno del server"));
    }
}
