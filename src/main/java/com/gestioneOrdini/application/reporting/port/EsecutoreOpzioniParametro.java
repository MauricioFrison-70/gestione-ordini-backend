package com.gestioneOrdini.application.reporting.port;

import com.gestioneOrdini.application.reporting.dto.OpzioneParametroResponse;

import java.util.List;

public interface EsecutoreOpzioniParametro {
    List<OpzioneParametroResponse> eseguire(String nomeProcedura);
}
