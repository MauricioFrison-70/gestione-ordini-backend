package com.gestioneOrdini.presentation;

import com.gestioneOrdini.domain.agente.model.TipoAgente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TipoAgenteController.class)
class TipoAgenteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void dovrebbeRestituireTuttiITipiDiAgente() throws Exception {
        mockMvc.perform(get("/api/tipo-agente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(TipoAgente.values().length))
                .andExpect(jsonPath("$[0]").value(TipoAgente.CLIENTE.name()))
                .andExpect(jsonPath("$[1]").value(TipoAgente.TRASPORTATORE.name()))
                .andExpect(jsonPath("$[2]").value(TipoAgente.FORNITORE.name()))
                .andExpect(jsonPath("$[3]").value(TipoAgente.VENDITORE.name()));
    }
}
