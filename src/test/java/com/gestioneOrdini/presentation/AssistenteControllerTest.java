package com.gestioneOrdini.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestioneOrdini.application.assistente.dto.DomandaAssistenteRequest;
import com.gestioneOrdini.application.assistente.dto.RispostaAssistenteResponse;
import com.gestioneOrdini.application.assistente.usecase.RispondiDomandaAssistenteUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssistenteController.class)
class AssistenteControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean RispondiDomandaAssistenteUseCase useCase;

    @Test
    void dovrebbeRispondereAllaDomanda() throws Exception {
        when(useCase.eseguire(any(DomandaAssistenteRequest.class)))
                .thenReturn(new RispostaAssistenteResponse("La giacenza è aggiornata dagli ordini."));

        mockMvc.perform(post("/api/assistente/domande")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "domanda", "Come viene aggiornata la giacenza?",
                                "cronologia", List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.risposta")
                        .value("La giacenza è aggiornata dagli ordini."));

        verify(useCase).eseguire(any(DomandaAssistenteRequest.class));
    }

    @Test
    void dovrebbeRifiutareUnaDomandaVuota() throws Exception {
        mockMvc.perform(post("/api/assistente/domande")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"domanda\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.domanda").value("La domanda è obbligatoria"));
    }

    @Test
    void dovrebbeValidareAncheLaCronologia() throws Exception {
        mockMvc.perform(post("/api/assistente/domande")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"domanda":"Continua", "cronologia":[
                                  {"ruolo":"UTENTE", "contenuto":""}
                                ]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['cronologia[0].contenuto']")
                        .value("Il contenuto è obbligatorio"));
    }
}
