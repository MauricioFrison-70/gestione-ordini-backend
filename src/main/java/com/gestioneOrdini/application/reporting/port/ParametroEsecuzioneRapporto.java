package com.gestioneOrdini.application.reporting.port;

import com.gestioneOrdini.domain.reporting.model.TipoParametroRapporto;

public record ParametroEsecuzioneRapporto(
        String nome,
        String tipoSql,
        TipoParametroRapporto tipo,
        Object valore
) {
}
