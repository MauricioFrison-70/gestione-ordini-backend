package com.gestioneOrdini.application.reporting.port;

import com.gestioneOrdini.application.reporting.dto.EsecuzioneRapportoResponse;
import com.gestioneOrdini.domain.reporting.model.Rapporto;

import java.util.List;

public interface EsecutoreRapporto {
    EsecuzioneRapportoResponse eseguire(
            Rapporto rapporto,
            List<ParametroEsecuzioneRapporto> parametri
    );
}
