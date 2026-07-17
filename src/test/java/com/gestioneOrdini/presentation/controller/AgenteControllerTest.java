package com.gestioneOrdini.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestioneOrdini.application.dto.AgenteRequest;
import com.gestioneOrdini.application.dto.AgenteResponse;
import com.gestioneOrdini.domain.model.TipoAgente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AgenteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private AgenteRequest creaAgenteRequestFittizio() {
        return new AgenteRequest(
                "Maurizio Test",
                "maurizio@test.com",
                TipoAgente.CLIENTE,
                true
        );
    }

    private Long creaAgenteERestituisciId() throws Exception {
        AgenteRequest request = creaAgenteRequestFittizio();

        String response = mockMvc.perform(
                        post("/api/agenti")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    // ---------------------------------------------------------
    // 1. Criar
    // ---------------------------------------------------------
    @Test
    void deveCreareAgente() throws Exception {
        int randomNumber = ThreadLocalRandom.current().nextInt(1000, 9999);

        AgenteRequest request = creaAgenteRequestFittizio();
        String nomeComRandom = request.getNome() + " " + randomNumber;
        request.setNome(nomeComRandom);

        mockMvc.perform(
                        post("/api/agenti")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value(nomeComRandom))
                .andExpect(jsonPath("$.email").value("maurizio@test.com"))
                .andExpect(jsonPath("$.tipoAgente").value("CLIENTE"));
    }

    // ---------------------------------------------------------
    // 2. Buscar por ID
    // ---------------------------------------------------------
    @Test
    void deveRecuperareAgentePerId() throws Exception {
        Long id = creaAgenteERestituisciId();

        mockMvc.perform(get("/api/agenti/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value("Maurizio Test"));
    }

    // ---------------------------------------------------------
    // 3. Listar
    // ---------------------------------------------------------
    @Test
    void deveElencareAgenti() throws Exception {
        creaAgenteERestituisciId();

        mockMvc.perform(get("/api/agenti"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nome").exists());
    }

    // ---------------------------------------------------------
    // 4. Atualizar
    // ---------------------------------------------------------
    @Test
    void deveAggiornareAgente() throws Exception {
        Long id = creaAgenteERestituisciId();

        AgenteRequest request = creaAgenteRequestFittizio();
        request.setNome("Nome aggiornato");
        request.setArchiviato(false);

        mockMvc.perform(
                        put("/api/agenti/" + id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome aggiornato"));
    }

    // ---------------------------------------------------------
    // 5. Excluir
    // ---------------------------------------------------------
    @Test
    void deveEliminareAgente() throws Exception {
        Long id = creaAgenteERestituisciId();

        mockMvc.perform(delete("/api/agenti/" + id))
                .andExpect(status().isNoContent());

        // Verifica que não existe mais
        mockMvc.perform(get("/api/agenti/" + id))
                .andExpect(status().isNotFound());
    }
}

