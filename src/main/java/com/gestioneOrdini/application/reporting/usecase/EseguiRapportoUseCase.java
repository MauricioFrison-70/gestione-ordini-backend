package com.gestioneOrdini.application.reporting.usecase;

import com.gestioneOrdini.application.reporting.dto.EsecuzioneRapportoResponse;
import com.gestioneOrdini.application.reporting.port.EsecutoreRapporto;
import com.gestioneOrdini.application.reporting.port.ParametroEsecuzioneRapporto;
import com.gestioneOrdini.domain.reporting.model.ParametroRapporto;
import com.gestioneOrdini.domain.reporting.model.Rapporto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EseguiRapportoUseCase {

    private final GetRapportoUseCase getRapportoUseCase;
    private final EsecutoreRapporto esecutore;

    public EseguiRapportoUseCase(GetRapportoUseCase getRapportoUseCase,
                                 EsecutoreRapporto esecutore) {
        this.getRapportoUseCase = getRapportoUseCase;
        this.esecutore = esecutore;
    }

    public EsecuzioneRapportoResponse eseguire(Long rapportoId, Map<String, Object> valori) {
        Rapporto rapporto = getRapportoUseCase.eseguire(rapportoId);
        Map<String, Object> parametriRicevuti = valori == null ? Map.of() : valori;
        validaParametriSconosciuti(rapporto, parametriRicevuti.keySet());

        List<ParametroEsecuzioneRapporto> parametri = rapporto.getParametri().stream()
                .map(parametro -> converte(parametro, parametriRicevuti.get(parametro.getNome())))
                .toList();
        return esecutore.eseguire(rapporto, parametri);
    }

    private void validaParametriSconosciuti(Rapporto rapporto, Set<String> nomiRicevuti) {
        Set<String> consentiti = rapporto.getParametri().stream()
                .map(ParametroRapporto::getNome)
                .collect(Collectors.toSet());
        nomiRicevuti.stream()
                .filter(nome -> !consentiti.contains(nome))
                .findFirst()
                .ifPresent(nome -> {
                    throw new IllegalArgumentException("Parametro non riconosciuto: " + nome);
                });
    }

    private ParametroEsecuzioneRapporto converte(ParametroRapporto parametro, Object valore) {
        if (valore == null || valore instanceof String testo && testo.isBlank()) {
            if (parametro.getValorePredefinito() != null
                    && !parametro.getValorePredefinito().isBlank()) {
                valore = parametro.getValorePredefinito();
            } else {
            if (parametro.isObbligatorio()) {
                throw new IllegalArgumentException(
                        "Il parametro '" + parametro.getEtichetta() + "' è obbligatorio");
            }
            return new ParametroEsecuzioneRapporto(
                    parametro.getNome(), parametro.getTipoSql(), parametro.getTipo(), null);
            }
        }

        try {
            Object convertito = switch (parametro.getTipo()) {
                case DATA -> LocalDate.parse(valore.toString());
                case INTERO -> interoEsatto(valore).intValueExact();
                case SELEZIONE -> selezione(valore, parametro.getTipoSql());
                case DECIMALE -> new BigDecimal(valore.toString());
                case BOOLEANO -> booleano(valore);
                case TESTO -> testo(valore);
            };
            return new ParametroEsecuzioneRapporto(
                    parametro.getNome(), parametro.getTipoSql(), parametro.getTipo(), convertito);
        } catch (ArithmeticException | NumberFormatException | DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "Il parametro '" + parametro.getEtichetta() + "' non ha un formato valido");
        }
    }

    private BigDecimal interoEsatto(Object valore) {
        return new BigDecimal(valore.toString()).stripTrailingZeros();
    }

    private Object selezione(Object valore, String tipoSql) {
        return switch (tipoSql) {
            case "tinyint", "smallint", "int", "bigint" ->
                    interoEsatto(valore).longValueExact();
            case "decimal", "numeric", "money", "smallmoney" ->
                    new BigDecimal(valore.toString());
            default -> testo(valore);
        };
    }

    private Boolean booleano(Object valore) {
        if (valore instanceof Boolean booleano) {
            return booleano;
        }
        if ("true".equalsIgnoreCase(valore.toString())) return true;
        if ("false".equalsIgnoreCase(valore.toString())) return false;
        throw new IllegalArgumentException("Il valore booleano non è valido");
    }

    private String testo(Object valore) {
        String testo = valore.toString().trim();
        if (testo.length() > 500) {
            throw new IllegalArgumentException("Il testo supera 500 caratteri");
        }
        return testo;
    }
}
