package com.gestioneOrdini.application.reporting.usecase;

import com.gestioneOrdini.application.reporting.dto.OpzioneParametroResponse;
import com.gestioneOrdini.application.reporting.port.EsecutoreOpzioniParametro;
import com.gestioneOrdini.domain.reporting.model.ParametroRapporto;
import com.gestioneOrdini.domain.reporting.model.Rapporto;
import com.gestioneOrdini.domain.reporting.model.TipoParametroRapporto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListOpzioniParametroUseCaseTest {

    @Test
    void dovrebbeCaricareLeOpzioniDallaProceduraConfigurataNelDatabase() {
        GetRapportoUseCase getRapporto = mock(GetRapportoUseCase.class);
        EsecutoreOpzioniParametro esecutore = mock(EsecutoreOpzioniParametro.class);
        Rapporto rapporto = new Rapporto(1L, "TEST", "Test", "",
                "reporting.usp_test", true, List.of(new ParametroRapporto(
                1L, "ClienteId", "Cliente", "bigint",
                TipoParametroRapporto.SELEZIONE, false, 1,
                null, "reporting.usp_opzioni_clienti")));
        when(getRapporto.eseguire(1L)).thenReturn(rapporto);
        when(esecutore.eseguire("reporting.usp_opzioni_clienti"))
                .thenReturn(List.of(new OpzioneParametroResponse(10L, "Cliente Uno")));

        List<OpzioneParametroResponse> risultato =
                new ListOpzioniParametroUseCase(getRapporto, esecutore)
                        .eseguire(1L, "ClienteId");

        assertThat(risultato).containsExactly(
                new OpzioneParametroResponse(10L, "Cliente Uno"));
    }
}
