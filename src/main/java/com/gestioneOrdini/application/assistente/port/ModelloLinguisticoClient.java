package com.gestioneOrdini.application.assistente.port;

import java.util.List;

public interface ModelloLinguisticoClient {
    ValutazioneRisposta rispondi(
            String istruzioni,
            List<MessaggioModello> cronologia,
            String domanda);

    record MessaggioModello(String ruolo, String contenuto) {
    }

    record ValutazioneRisposta(boolean inAmbito, String risposta) {
    }
}
