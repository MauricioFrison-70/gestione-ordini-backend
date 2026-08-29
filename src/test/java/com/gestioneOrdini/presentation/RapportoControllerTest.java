package com.gestioneOrdini.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestioneOrdini.application.reporting.dto.ColonnaRapportoResponse;
import com.gestioneOrdini.application.reporting.dto.EsecuzioneRapportoResponse;
import com.gestioneOrdini.application.reporting.dto.OpzioneParametroResponse;
import com.gestioneOrdini.application.reporting.usecase.EseguiRapportoUseCase;
import com.gestioneOrdini.application.reporting.usecase.GetRapportoUseCase;
import com.gestioneOrdini.application.reporting.usecase.ListRapportiUseCase;
import com.gestioneOrdini.application.reporting.usecase.ListOpzioniParametroUseCase;
import com.gestioneOrdini.domain.reporting.model.ParametroRapporto;
import com.gestioneOrdini.domain.reporting.model.Rapporto;
import com.gestioneOrdini.domain.reporting.model.TipoParametroRapporto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RapportoController.class)
class RapportoControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean ListRapportiUseCase listUseCase;
    @MockBean GetRapportoUseCase getUseCase;
    @MockBean EseguiRapportoUseCase eseguiUseCase;
    @MockBean ListOpzioniParametroUseCase listOpzioniUseCase;

    private Rapporto rapporto;

    @BeforeEach
    void setUp() {
        rapporto = new Rapporto(1L, "ORDINI", "Ordini di vendita", "Descrizione",
                "reporting.usp_ordini", true, List.of(
                new ParametroRapporto("DataInizio", "Data iniziale",
                        TipoParametroRapporto.DATA, true, 1)));
    }

    @Test
    void dovrebbeElencareIRapportiConLaTipizzazioneDeiParametri() throws Exception {
        when(listUseCase.eseguire()).thenReturn(List.of(rapporto));

        mockMvc.perform(get("/api/rapporti"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codice").value("ORDINI"))
                .andExpect(jsonPath("$[0].parametri[0].tipo").value("DATA"));
    }

    @Test
    void dovrebbeRestituireDettaglioEParametri() throws Exception {
        when(getUseCase.eseguire(1L)).thenReturn(rapporto);

        mockMvc.perform(get("/api/rapporti/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titolo").value("Ordini di vendita"));
        mockMvc.perform(get("/api/rapporti/1/parametri"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("DataInizio"));
    }

    @Test
    void dovrebbeEseguireIlRapporto() throws Exception {
        EsecuzioneRapportoResponse risultato = new EsecuzioneRapportoResponse(
                List.of(new ColonnaRapportoResponse(
                        "numeroOrdine", "Numero ordine", "TESTO", null, false),
                        new ColonnaRapportoResponse(
                                "valoreTotale", "Valore totale", "DECIMALE", "VALUTA", true)),
                List.of(Map.of(
                        "numeroOrdine", "OV2026000001",
                        "valoreTotale", new BigDecimal("12.50"))),
                Map.of("valoreTotale", new BigDecimal("12.50")), 1, false);
        when(eseguiUseCase.eseguire(eq(1L), eq(Map.of("DataInizio", "2026-08-01"))))
                .thenReturn(risultato);

        mockMvc.perform(post("/api/rapporti/1/esegui")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "parametri", Map.of("DataInizio", "2026-08-01")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.colonne[0].tipo").value("TESTO"))
                .andExpect(jsonPath("$.colonne[1].totalizzare").value(true))
                .andExpect(jsonPath("$.righe[0].numeroOrdine").value("OV2026000001"))
                .andExpect(jsonPath("$.totali.valoreTotale").value(12.5))
                .andExpect(jsonPath("$.totaleRighe").value(1))
                .andExpect(jsonPath("$.troncato").value(false));
    }

    @Test
    void dovrebbeRestituireLeOpzioniDiUnParametroDiSelezione() throws Exception {
        when(listOpzioniUseCase.eseguire(1L, "ClienteId"))
                .thenReturn(List.of(new OpzioneParametroResponse(10L, "Cliente Uno")));

        mockMvc.perform(get("/api/rapporti/1/parametri/ClienteId/opzioni"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].valore").value(10))
                .andExpect(jsonPath("$[0].etichetta").value("Cliente Uno"));
    }

    @Test
    void dovrebbeRifiutareRichiestaSenzaParametri() throws Exception {
        mockMvc.perform(post("/api/rapporti/1/esegui")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.parametri").value("I parametri sono obbligatori"));
    }
}
