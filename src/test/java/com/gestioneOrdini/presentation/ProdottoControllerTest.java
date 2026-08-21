package com.gestioneOrdini.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestioneOrdini.application.prodotto.dto.ProdottoRequest;
import com.gestioneOrdini.application.prodotto.dto.ProdottoResponse;
import com.gestioneOrdini.application.prodotto.dto.ProdottoUpdateRequest;
import com.gestioneOrdini.application.prodotto.usecase.*;
import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import com.gestioneOrdini.domain.prodotto.exception.CodiceProdottoDuplicatoException;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import com.gestioneOrdini.infrastructure.persistence.mapper.prodotto.ProdottoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProdottoController.class)
class ProdottoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapperJson;

    @MockBean
    private CreateProdottoUseCase createUseCase;
    @MockBean
    private UpdateProdottoUseCase updateUseCase;
    @MockBean
    private DeleteProdottoUseCase deleteUseCase;
    @MockBean
    private GetProdottoUseCase getUseCase;
    @MockBean
    private ListProdottiUseCase listUseCase;
    @MockBean
    private ProdottoMapper mapper;

    private Prodotto prodotto;
    private ProdottoResponse response;
    private ProdottoRequest request;
    private ProdottoUpdateRequest richiestaAggiornamento;

    @BeforeEach
    void setUp() {

        prodotto = new Prodotto(
                1L,
                "P001",
                "Notebook Dell",
                new BigDecimal("1500.00"),
                new BigDecimal("2200.00"),
                10,
                2,
                false
        );

        response = new ProdottoResponse(
                1L,
                "P001",
                "Notebook Dell",
                new BigDecimal("1500.00"),
                new BigDecimal("2200.00"),
                10,
                2,
                false,
                LocalDateTime.now()
        );

        request = new ProdottoRequest(
                "P001",
                "Notebook Dell",
                new BigDecimal("1500.00"),
                new BigDecimal("2200.00"),
                10,
                2,
                false
        );

        richiestaAggiornamento = new ProdottoUpdateRequest(
                "Notebook Dell aggiornato",
                new BigDecimal("1500.00"),
                new BigDecimal("2200.00"),
                10,
                2,
                false
        );
    }

    @Test
    void deveCreareProdotto() throws Exception {

        Mockito.when(mapper.toDomain(any(ProdottoRequest.class))).thenReturn(prodotto);
        Mockito.when(createUseCase.eseguire(any())).thenReturn(prodotto);
        Mockito.when(mapper.toResponse(any())).thenReturn(response);

        mockMvc.perform(post("/api/prodotti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapperJson.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/prodotti/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.codice").value("P001"))
                .andExpect(jsonPath("$.descrizione").value("Notebook Dell"))
                .andExpect(jsonPath("$.valoreAcquisto").value(1500.00))
                .andExpect(jsonPath("$.valoreVendita").value(2200.00))
                .andExpect(jsonPath("$.quantita").value(10))
                .andExpect(jsonPath("$.scortaMinima").value(2))
                .andExpect(jsonPath("$.archiviato").value(false))
                .andExpect(jsonPath("$.dataRegistrazione").exists());
    }

    @Test
    void deveRestituireProdottoPerId() throws Exception {

        Mockito.when(getUseCase.eseguire(1L)).thenReturn(prodotto);
        Mockito.when(mapper.toResponse(prodotto)).thenReturn(response);

        mockMvc.perform(get("/api/prodotti/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.codice").value("P001"))
                .andExpect(jsonPath("$.descrizione").value("Notebook Dell"))
                .andExpect(jsonPath("$.valoreAcquisto").value(1500.00))
                .andExpect(jsonPath("$.valoreVendita").value(2200.00))
                .andExpect(jsonPath("$.quantita").value(10))
                .andExpect(jsonPath("$.scortaMinima").value(2))
                .andExpect(jsonPath("$.archiviato").value(false))
                .andExpect(jsonPath("$.dataRegistrazione").exists());
    }

    @Test
    void deveElencareProdotti() throws Exception {

        Mockito.when(listUseCase.eseguire()).thenReturn(List.of(prodotto));
        Mockito.when(mapper.toResponse(any())).thenReturn(response);

        mockMvc.perform(get("/api/prodotti"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].codice").value("P001"))
                .andExpect(jsonPath("$[0].descrizione").value("Notebook Dell"))
                .andExpect(jsonPath("$[0].valoreAcquisto").value(1500.00))
                .andExpect(jsonPath("$[0].valoreVendita").value(2200.00))
                .andExpect(jsonPath("$[0].quantita").value(10))
                .andExpect(jsonPath("$[0].scortaMinima").value(2))
                .andExpect(jsonPath("$[0].archiviato").value(false))
                .andExpect(jsonPath("$[0].dataRegistrazione").exists());
    }

    @Test
    void deveAggiornareProdotto() throws Exception {

        Mockito.when(getUseCase.eseguire(1L)).thenReturn(prodotto);
        Mockito.when(mapper.toDomain(any(ProdottoUpdateRequest.class), eq("P001"))).thenReturn(prodotto);
        Mockito.when(updateUseCase.eseguire(eq(1L), any())).thenReturn(prodotto);
        Mockito.when(mapper.toResponse(any())).thenReturn(response);

        mockMvc.perform(put("/api/prodotti/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapperJson.writeValueAsString(richiestaAggiornamento)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.codice").value("P001"))
                .andExpect(jsonPath("$.descrizione").value("Notebook Dell"))
                .andExpect(jsonPath("$.valoreAcquisto").value(1500.00))
                .andExpect(jsonPath("$.valoreVendita").value(2200.00))
                .andExpect(jsonPath("$.quantita").value(10))
                .andExpect(jsonPath("$.scortaMinima").value(2))
                .andExpect(jsonPath("$.archiviato").value(false))
                .andExpect(jsonPath("$.dataRegistrazione").exists());
    }

    @Test
    void deveEliminareProdotto() throws Exception {

        Mockito.doNothing().when(deleteUseCase).eseguire(1L);

        mockMvc.perform(delete("/api/prodotti/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveRitornareBadRequestQuandoRequestInvalido() throws Exception {

        ProdottoRequest invalido = new ProdottoRequest(
                "",
                "",
                null,
                null,
                null,
                null,
                false
        );

        mockMvc.perform(post("/api/prodotti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapperJson.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRitornareConflittoQuandoCodiceProdottoEDuplicato() throws Exception {
        Mockito.when(mapper.toDomain(any(ProdottoRequest.class))).thenReturn(prodotto);
        Mockito.when(createUseCase.eseguire(any()))
                .thenThrow(new CodiceProdottoDuplicatoException("P001", new RuntimeException()));

        mockMvc.perform(post("/api/prodotti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapperJson.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errore").value("Esiste già un prodotto con il codice 'P001'."));
    }

    @Test
    void deveRitornareBadRequestQuandoCodiceODescrizioneSuperanoILimite() throws Exception {
        ProdottoRequest codiceTroppoLungo = new ProdottoRequest(
                "ABC1234",
                "Descrizione valida",
                new BigDecimal("10.00"),
                new BigDecimal("20.00"),
                1,
                0,
                false
        );
        ProdottoRequest descrizioneTroppoLunga = new ProdottoRequest(
                "ABC123",
                "D".repeat(31),
                new BigDecimal("10.00"),
                new BigDecimal("20.00"),
                1,
                0,
                false
        );

        mockMvc.perform(post("/api/prodotti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapperJson.writeValueAsString(codiceTroppoLungo)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/prodotti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapperJson.writeValueAsString(descrizioneTroppoLunga)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRitornareBadRequestQuandoDescrizioneDiAggiornamentoEVuota() throws Exception {

        ProdottoUpdateRequest invalido = new ProdottoUpdateRequest(
                "",
                new BigDecimal("1500.00"),
                new BigDecimal("2200.00"),
                10,
                2,
                false
        );

        mockMvc.perform(put("/api/prodotti/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapperJson.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRitornareBadRequestQuandoDescrizioneDiAggiornamentoSuperaILimite() throws Exception {
        ProdottoUpdateRequest invalido = new ProdottoUpdateRequest(
                "D".repeat(31),
                new BigDecimal("1500.00"),
                new BigDecimal("2200.00"),
                10,
                2,
                false
        );

        mockMvc.perform(put("/api/prodotti/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapperJson.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRitornareBadRequestQuandoValoreDiAggiornamentoENegativo() throws Exception {

        ProdottoUpdateRequest invalido = new ProdottoUpdateRequest(
                "Notebook Dell aggiornato",
                new BigDecimal("-0.01"),
                new BigDecimal("2200.00"),
                10,
                2,
                false
        );

        mockMvc.perform(put("/api/prodotti/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapperJson.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRitornareBadRequestQuandoMancaCampoObbligatorioNellAggiornamento() throws Exception {

        String richiestaSenzaDescrizione = """
                {
                  "valoreAcquisto": 1500.00,
                  "valoreVendita": 2200.00,
                  "quantita": 10,
                  "scortaMinima": 2,
                  "archiviato": false
                }
                """;

        mockMvc.perform(put("/api/prodotti/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(richiestaSenzaDescrizione))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveIgnorareCodiceInviatoNellAggiornamento() throws Exception {

        String richiestaConCodice = """
                {
                  "codice": "P999",
                  "descrizione": "Notebook Dell aggiornato",
                  "valoreAcquisto": 1500.00,
                  "valoreVendita": 2200.00,
                  "quantita": 10,
                  "scortaMinima": 2,
                  "archiviato": false
                }
                """;

        Mockito.when(getUseCase.eseguire(1L)).thenReturn(prodotto);
        Mockito.when(mapper.toDomain(any(ProdottoUpdateRequest.class), eq("P001"))).thenReturn(prodotto);
        Mockito.when(updateUseCase.eseguire(1L, prodotto)).thenReturn(prodotto);
        Mockito.when(mapper.toResponse(prodotto)).thenReturn(response);

        mockMvc.perform(put("/api/prodotti/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(richiestaConCodice))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codice").value("P001"));

        Mockito.verify(mapper).toDomain(any(ProdottoUpdateRequest.class), eq("P001"));
    }

    @Test
    void deveRitornareNotFoundQuandoProdottoNonEsiste() throws Exception {

        Mockito.when(getUseCase.eseguire(999L))
                .thenThrow(new EntityNotFoundException("Prodotto", 999L));

        mockMvc.perform(get("/api/prodotti/999"))
                .andExpect(status().isNotFound());
    }
}
