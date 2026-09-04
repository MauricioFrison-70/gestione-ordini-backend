package com.gestioneOrdini.application.assistente.usecase;

import com.gestioneOrdini.application.assistente.dto.DomandaAssistenteRequest;
import com.gestioneOrdini.application.assistente.dto.MessaggioConversazioneRequest;
import com.gestioneOrdini.application.assistente.port.BaseConoscenzaAssistente;
import com.gestioneOrdini.application.assistente.port.ContestoDatiOrdiniVendita;
import com.gestioneOrdini.application.assistente.port.ModelloLinguisticoClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RispondiDomandaAssistenteUseCaseTest {
    @Mock BaseConoscenzaAssistente baseConoscenza;
    @Mock ContestoDatiOrdiniVendita contestoDati;
    @Mock ModelloLinguisticoClient modello;
    private RispondiDomandaAssistenteUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RispondiDomandaAssistenteUseCase(baseConoscenza, contestoDati, modello);
        when(baseConoscenza.contenuto()).thenReturn("Gli ordini pendenti sono modificabili.");
        when(contestoDati.contenutoCorrente()).thenReturn(
                "[RIEPILOGO_GENERALE]\nnumeroOrdini|valoreTotale\n12|1540.00");
    }

    @Test
    void dovrebbeFornireAlModelloIValoriCorrentiLettiDalDatabase() {
        var request = new DomandaAssistenteRequest(
                "Quanti ordini sono registrati?", List.of());
        when(modello.rispondi(
                contains("12|1540.00"), anyList(), eq(request.domanda())))
                .thenReturn(new ModelloLinguisticoClient.ValutazioneRisposta(
                        true, "Sono registrati 12 ordini."));

        var risposta = useCase.eseguire(request);

        assertThat(risposta.risposta()).isEqualTo("Sono registrati 12 ordini.");
    }

    @Test
    void dovrebbeRispondereAlleDomandeSulSistemaConLaCronologia() {
        var request = new DomandaAssistenteRequest(
                "E dopo il rilascio?",
                List.of(new MessaggioConversazioneRequest(
                        MessaggioConversazioneRequest.RuoloMessaggio.UTENTE,
                        "Quando posso modificare un ordine?")));
        when(modello.rispondi(contains("Gli ordini pendenti"), anyList(), eq("E dopo il rilascio?")))
                .thenReturn(new ModelloLinguisticoClient.ValutazioneRisposta(
                        true, "Dopo il rilascio non è più modificabile."));

        var risposta = useCase.eseguire(request);

        assertThat(risposta.risposta()).isEqualTo("Dopo il rilascio non è più modificabile.");
    }

    @Test
    void dovrebbeImporreLaRispostaDiRifiutoQuandoLaDomandaENonPertinente() {
        var request = new DomandaAssistenteRequest("Qual è la capitale della Francia?", List.of());
        when(modello.rispondi(contains("Gli ordini pendenti"), anyList(), eq(request.domanda())))
                .thenReturn(new ModelloLinguisticoClient.ValutazioneRisposta(
                        false, "La capitale è Parigi."));

        var risposta = useCase.eseguire(request);

        assertThat(risposta.risposta())
                .isEqualTo(RispondiDomandaAssistenteUseCase.RISPOSTA_FUORI_AMBITO);
    }
}
