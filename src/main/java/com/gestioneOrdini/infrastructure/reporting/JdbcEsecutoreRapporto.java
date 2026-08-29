package com.gestioneOrdini.infrastructure.reporting;

import com.gestioneOrdini.application.reporting.dto.ColonnaRapportoResponse;
import com.gestioneOrdini.application.reporting.dto.EsecuzioneRapportoResponse;
import com.gestioneOrdini.application.reporting.dto.OpzioneParametroResponse;
import com.gestioneOrdini.application.reporting.port.EsecutoreOpzioniParametro;
import com.gestioneOrdini.application.reporting.port.EsecutoreRapporto;
import com.gestioneOrdini.application.reporting.port.ParametroEsecuzioneRapporto;
import com.gestioneOrdini.domain.reporting.model.ColonnaRapporto;
import com.gestioneOrdini.domain.reporting.model.Rapporto;
import com.gestioneOrdini.domain.reporting.model.TipoParametroRapporto;
import com.gestioneOrdini.infrastructure.config.ReportingExecutionProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@EnableConfigurationProperties(ReportingExecutionProperties.class)
public class JdbcEsecutoreRapporto implements EsecutoreRapporto, EsecutoreOpzioniParametro {

    private static final String PROCEDURA_CONSENTITA = "reporting\\.[A-Za-z_][A-Za-z0-9_]*";

    private final ReportingConnectionProvider connections;
    private final ReportingExecutionProperties executionProperties;

    public JdbcEsecutoreRapporto(ReportingConnectionProvider connections,
                                 ReportingExecutionProperties executionProperties) {
        this.connections = connections;
        this.executionProperties = executionProperties;
    }

    @Override
    public EsecuzioneRapportoResponse eseguire(
            Rapporto rapporto, List<ParametroEsecuzioneRapporto> parametri) {
        String nomeProcedura = rapporto.getNomeProcedura();
        validaNomeProcedura(nomeProcedura);
        String segnaposto = String.join(",", parametri.stream().map(p -> "?").toList());
        String chiamata = "{call " + nomeProcedura + "(" + segnaposto + ")}";

        try (Connection connection = connections.getConnection()) {
            validaFirmaProcedura(connection, nomeProcedura, parametri);
            try (CallableStatement statement = connection.prepareCall(chiamata)) {
                statement.setQueryTimeout(executionProperties.getQueryTimeoutSeconds());
                for (int indice = 0; indice < parametri.size(); indice++) {
                    impostaParametro(statement, indice + 1, parametri.get(indice));
                }
                ResultSet resultSet = primoRisultato(statement);
                if (resultSet == null) {
                    return new EsecuzioneRapportoResponse(
                            List.of(), List.of(), Map.of(), 0, false);
                }
                try (resultSet) {
                    return leggeRisultato(resultSet, rapporto.getColonne());
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Impossibile eseguire il rapporto richiesto", ex);
        }
    }

    @Override
    public List<OpzioneParametroResponse> eseguire(String nomeProcedura) {
        validaNomeProcedura(nomeProcedura);
        try (Connection connection = connections.getConnection();
             CallableStatement statement = connection.prepareCall(
                     "{call " + nomeProcedura + "()}")) {
            statement.setQueryTimeout(executionProperties.getQueryTimeoutSeconds());
            ResultSet resultSet = primoRisultato(statement);
            if (resultSet == null) return List.of();
            try (resultSet) {
                List<OpzioneParametroResponse> opzioni = new ArrayList<>();
                int massimo = Math.max(1, executionProperties.getMaximumRows());
                while (resultSet.next() && opzioni.size() < massimo) {
                    opzioni.add(new OpzioneParametroResponse(
                            valoreJson(resultSet.getObject("valore")),
                            resultSet.getString("etichetta")));
                }
                return opzioni;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Impossibile caricare le opzioni del parametro", ex);
        }
    }

    private ResultSet primoRisultato(CallableStatement statement) throws SQLException {
        boolean contieneRisultato = statement.execute();
        while (!contieneRisultato && statement.getUpdateCount() != -1) {
            contieneRisultato = statement.getMoreResults();
        }
        return contieneRisultato ? statement.getResultSet() : null;
    }

    private EsecuzioneRapportoResponse leggeRisultato(
            ResultSet resultSet, List<ColonnaRapporto> configurazioni) throws SQLException {
        ResultSetMetaData metadata = resultSet.getMetaData();
        List<ColonnaLetta> colonne = preparaColonne(metadata, configurazioni);
        List<ColonnaRapportoResponse> rispostaColonne = colonne.stream()
                .map(colonna -> new ColonnaRapportoResponse(
                        colonna.nome(), colonna.etichetta(), colonna.tipo(), colonna.formato(),
                        colonna.totalizzare()))
                .toList();

        int massimo = Math.max(1, executionProperties.getMaximumRows());
        List<Map<String, Object>> righe = new ArrayList<>();
        Map<String, BigDecimal> totali = new LinkedHashMap<>();
        colonne.stream().filter(ColonnaLetta::totalizzare)
                .forEach(colonna -> totali.put(colonna.nome(), BigDecimal.ZERO));
        boolean troncato = false;
        while (resultSet.next()) {
            if (righe.size() >= massimo) {
                troncato = true;
                break;
            }
            Map<String, Object> riga = new LinkedHashMap<>();
            for (ColonnaLetta colonna : colonne) {
                Object valore = valoreJson(resultSet.getObject(colonna.indice()));
                riga.put(colonna.nome(), valore);
                if (colonna.totalizzare() && valore != null) {
                    totali.merge(colonna.nome(), valoreNumerico(valore), BigDecimal::add);
                }
            }
            righe.add(riga);
        }
        return new EsecuzioneRapportoResponse(
                rispostaColonne, righe, totali, righe.size(), troncato);
    }

    private List<ColonnaLetta> preparaColonne(
            ResultSetMetaData metadata, List<ColonnaRapporto> configurazioni) throws SQLException {
        Map<String, ColonnaRapporto> perNome = new HashMap<>();
        configurazioni.forEach(colonna ->
                perNome.put(colonna.getNome().toLowerCase(Locale.ROOT), colonna));

        List<ColonnaLetta> colonne = new ArrayList<>();
        for (int indice = 1; indice <= metadata.getColumnCount(); indice++) {
            String nome = metadata.getColumnLabel(indice);
            ColonnaRapporto configurazione = perNome.get(nome.toLowerCase(Locale.ROOT));
            if (configurazione == null || !configurazione.isVisibile()) continue;
            String tipo = tipoColonna(metadata.getColumnType(indice));
            boolean totalizzare = configurazione.isTotalizzare();
            if (totalizzare && !tipo.equals("INTERO") && !tipo.equals("DECIMALE")) {
                throw new IllegalStateException(
                        "La colonna '" + nome + "' può essere totalizzata solo se numerica");
            }
            colonne.add(new ColonnaLetta(
                    indice,
                    nome,
                    configurazione.getEtichetta(),
                    tipo,
                    configurazione.getFormato(),
                    configurazione.getOrdine(),
                    totalizzare));
        }
        colonne.sort(Comparator.comparingInt(ColonnaLetta::ordine));
        return colonne;
    }

    private void validaFirmaProcedura(Connection connection, String nomeProcedura,
                                       List<ParametroEsecuzioneRapporto> parametri)
            throws SQLException {
        String sql = """
                SELECT SUBSTRING(p.name, 2, 128) AS nome, p.is_output
                FROM sys.parameters p
                WHERE p.object_id = OBJECT_ID(?) AND p.parameter_id > 0
                ORDER BY p.parameter_id
                """;
        List<String> nomiDatabase = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nomeProcedura);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    if (resultSet.getBoolean("is_output")) {
                        throw new IllegalStateException(
                                "Le procedure dei rapporti non possono avere parametri OUTPUT");
                    }
                    nomiDatabase.add(resultSet.getString("nome"));
                }
            }
        }
        List<String> nomiCatalogo = parametri.stream()
                .map(ParametroEsecuzioneRapporto::nome).toList();
        if (nomiDatabase.size() != nomiCatalogo.size()) {
            throw configurazioneNonSincronizzata();
        }
        for (int indice = 0; indice < nomiDatabase.size(); indice++) {
            if (!nomiDatabase.get(indice).equalsIgnoreCase(nomiCatalogo.get(indice))) {
                throw configurazioneNonSincronizzata();
            }
        }
    }

    private IllegalStateException configurazioneNonSincronizzata() {
        return new IllegalStateException(
                "I parametri del rapporto non corrispondono alla stored procedure. "
                        + "Eseguire reporting.usp_sincronizza_parametri.");
    }

    private void impostaParametro(CallableStatement statement, int indice,
                                  ParametroEsecuzioneRapporto parametro) throws SQLException {
        int tipoSql = tipoSql(parametro.tipo(), parametro.tipoSql());
        if (parametro.valore() == null) {
            statement.setNull(indice, tipoSql);
            return;
        }
        switch (parametro.tipo()) {
            case DATA -> statement.setDate(
                    indice, Date.valueOf((java.time.LocalDate) parametro.valore()));
            case INTERO -> statement.setInt(indice, (Integer) parametro.valore());
            case SELEZIONE -> statement.setObject(indice, parametro.valore(), tipoSql);
            case DECIMALE -> statement.setBigDecimal(indice, (BigDecimal) parametro.valore());
            case BOOLEANO -> statement.setBoolean(indice, (Boolean) parametro.valore());
            case TESTO -> statement.setString(indice, (String) parametro.valore());
        }
    }

    private int tipoSql(TipoParametroRapporto tipo, String nomeTipoSql) {
        if (tipo == TipoParametroRapporto.SELEZIONE) {
            return switch (nomeTipoSql) {
                case "tinyint" -> Types.TINYINT;
                case "smallint" -> Types.SMALLINT;
                case "int" -> Types.INTEGER;
                case "bigint" -> Types.BIGINT;
                case "decimal", "numeric", "money", "smallmoney" -> Types.DECIMAL;
                default -> Types.NVARCHAR;
            };
        }
        return switch (tipo) {
            case DATA -> Types.DATE;
            case INTERO -> Types.INTEGER;
            case DECIMALE -> Types.DECIMAL;
            case BOOLEANO -> Types.BOOLEAN;
            case TESTO -> Types.NVARCHAR;
            case SELEZIONE -> throw new IllegalStateException("Tipo di selezione non risolto");
        };
    }

    private String tipoColonna(int tipoSql) {
        return switch (tipoSql) {
            case Types.TINYINT, Types.SMALLINT, Types.INTEGER, Types.BIGINT -> "INTERO";
            case Types.NUMERIC, Types.DECIMAL, Types.FLOAT, Types.REAL, Types.DOUBLE -> "DECIMALE";
            case Types.DATE -> "DATA";
            case Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE -> "DATA_ORA";
            case Types.BOOLEAN, Types.BIT -> "BOOLEANO";
            default -> "TESTO";
        };
    }

    private Object valoreJson(Object valore) {
        if (valore instanceof Date data) return data.toLocalDate();
        if (valore instanceof Timestamp dataOra) return dataOra.toLocalDateTime();
        if (valore instanceof Time ora) return ora.toLocalTime();
        return valore;
    }

    private BigDecimal valoreNumerico(Object valore) {
        if (valore instanceof BigDecimal decimale) return decimale;
        if (valore instanceof Number numero) return new BigDecimal(numero.toString());
        throw new IllegalStateException("Il valore da totalizzare non è numerico");
    }

    private void validaNomeProcedura(String nomeProcedura) {
        if (nomeProcedura == null || !nomeProcedura.matches(PROCEDURA_CONSENTITA)) {
            throw new IllegalArgumentException("Procedura di rapporto non consentita");
        }
    }

    private record ColonnaLetta(
            int indice, String nome, String etichetta, String tipo, String formato, int ordine,
            boolean totalizzare) {
    }
}
