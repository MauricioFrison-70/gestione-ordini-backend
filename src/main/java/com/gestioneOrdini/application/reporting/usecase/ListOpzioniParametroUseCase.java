package com.gestioneOrdini.application.reporting.usecase;

import com.gestioneOrdini.application.reporting.dto.OpzioneParametroResponse;
import com.gestioneOrdini.application.reporting.port.EsecutoreOpzioniParametro;
import com.gestioneOrdini.domain.reporting.model.ParametroRapporto;
import com.gestioneOrdini.domain.reporting.model.Rapporto;
import com.gestioneOrdini.domain.reporting.model.TipoParametroRapporto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListOpzioniParametroUseCase {

    private final GetRapportoUseCase getRapportoUseCase;
    private final EsecutoreOpzioniParametro esecutore;

    public ListOpzioniParametroUseCase(GetRapportoUseCase getRapportoUseCase,
                                       EsecutoreOpzioniParametro esecutore) {
        this.getRapportoUseCase = getRapportoUseCase;
        this.esecutore = esecutore;
    }

    public List<OpzioneParametroResponse> eseguire(Long rapportoId, String nomeParametro) {
        Rapporto rapporto = getRapportoUseCase.eseguire(rapportoId);
        ParametroRapporto parametro = rapporto.getParametri().stream()
                .filter(item -> item.getNome().equals(nomeParametro))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Parametro non riconosciuto: " + nomeParametro));
        if (parametro.getTipo() != TipoParametroRapporto.SELEZIONE
                || parametro.getProceduraOpzioni() == null) {
            throw new IllegalArgumentException(
                    "Il parametro selezionato non dispone di opzioni configurate");
        }
        return esecutore.eseguire(parametro.getProceduraOpzioni());
    }
}
