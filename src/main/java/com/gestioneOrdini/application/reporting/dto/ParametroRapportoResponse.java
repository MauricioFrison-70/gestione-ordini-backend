package com.gestioneOrdini.application.reporting.dto;

import com.gestioneOrdini.domain.reporting.model.ParametroRapporto;
import com.gestioneOrdini.domain.reporting.model.TipoParametroRapporto;

public record ParametroRapportoResponse(
        String nome,
        String etichetta,
        TipoParametroRapporto tipo,
        boolean obbligatorio,
        int ordine,
        String valorePredefinito,
        boolean haOpzioni
) {
    public static ParametroRapportoResponse from(ParametroRapporto parametro) {
        return new ParametroRapportoResponse(parametro.getNome(), parametro.getEtichetta(),
                parametro.getTipo(), parametro.isObbligatorio(), parametro.getOrdine(),
                parametro.getValorePredefinito(), parametro.getProceduraOpzioni() != null);
    }
}
