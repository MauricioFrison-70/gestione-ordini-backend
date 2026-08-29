package com.gestioneOrdini.application.reporting.usecase;

import com.gestioneOrdini.application.reporting.dto.EsecuzioneRapportoResponse;
import com.gestioneOrdini.application.reporting.port.EsecutoreRapporto;
import com.gestioneOrdini.application.reporting.port.ParametroEsecuzioneRapporto;
import com.gestioneOrdini.domain.reporting.model.ParametroRapporto;
import com.gestioneOrdini.domain.reporting.model.Rapporto;
import com.gestioneOrdini.domain.reporting.model.TipoParametroRapporto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EseguiRapportoUseCaseTest {

    private GetRapportoUseCase getUseCase;
    private EsecutoreRapporto esecutore;
    private EseguiRapportoUseCase useCase;

    @BeforeEach
    void setUp() {
        getUseCase = mock(GetRapportoUseCase.class);
        esecutore = mock(EsecutoreRapporto.class);
        useCase = new EseguiRapportoUseCase(getUseCase, esecutore);
        when(getUseCase.eseguire(1L)).thenReturn(rapporto());
        when(esecutore.eseguire(any(Rapporto.class), anyList()))
                .thenReturn(new EsecuzioneRapportoResponse(
                        List.of(), List.of(), Map.of(), 0, false));
    }

    @Test
    void dovrebbeConvertireIParametriSecondoIlTipoDichiarato() {
        useCase.eseguire(1L, Map.of(
                "DataInizio", "2026-08-01",
                "ClienteId", 25));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ParametroEsecuzioneRapporto>> captor = ArgumentCaptor.forClass(List.class);
        verify(esecutore).eseguire(any(Rapporto.class), captor.capture());
        assertThat(captor.getValue()).extracting(ParametroEsecuzioneRapporto::valore)
                .containsExactly(LocalDate.of(2026, 8, 1), 25L);
    }

    @Test
    void dovrebbeRifiutareParametroObbligatorioAssente() {
        assertThatThrownBy(() -> useCase.eseguire(1L, Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Data iniziale");
    }

    @Test
    void dovrebbeRifiutareParametroNonCadastrato() {
        assertThatThrownBy(() -> useCase.eseguire(1L, Map.of(
                "DataInizio", "2026-08-01", "Sql", "DROP TABLE")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Parametro non riconosciuto");
    }

    @Test
    void dovrebbeRifiutareDataConFormatoInvalido() {
        assertThatThrownBy(() -> useCase.eseguire(1L, Map.of("DataInizio", "01/08/2026")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("formato valido");
    }

    private Rapporto rapporto() {
        return new Rapporto(1L, "TEST", "Test", "", "reporting.usp_test", true,
                List.of(
                        new ParametroRapporto("DataInizio", "Data iniziale",
                                TipoParametroRapporto.DATA, true, 1),
                        new ParametroRapporto("ClienteId", "Cliente",
                                TipoParametroRapporto.SELEZIONE, false, 2)));
    }
}
