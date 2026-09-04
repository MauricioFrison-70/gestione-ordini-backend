package com.gestioneOrdini.infrastructure.ai;

import com.gestioneOrdini.application.assistente.exception.AssistenteNonDisponibileException;
import com.gestioneOrdini.application.assistente.port.ContestoDatiOrdiniVendita;
import com.gestioneOrdini.infrastructure.config.AssistenteDatabaseProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "assistente.database", name = "enabled", havingValue = "true")
class JdbcContestoDatiOrdiniVendita implements ContestoDatiOrdiniVendita {
    private static final String VIEW = "reporting.vw_ordini_vendita";

    private static final List<Sezione> SEZIONI = List.of(
            new Sezione("RIEPILOGO_GENERALE", """
                    SELECT COUNT_BIG(*) AS numeroOrdini,
                           COALESCE(SUM(valoreTotale), 0) AS valoreTotale,
                           MIN(dataRegistrazione) AS primaRegistrazione,
                           MAX(dataRegistrazione) AS ultimaRegistrazione
                    FROM reporting.vw_ordini_vendita
                    """, false),
            new Sezione("PER_STATO", """
                    SELECT stato, COUNT_BIG(*) AS numeroOrdini,
                           COALESCE(SUM(valoreTotale), 0) AS valoreTotale
                    FROM reporting.vw_ordini_vendita
                    GROUP BY stato
                    ORDER BY stato
                    """, false),
            new Sezione("PER_VENDITORE", """
                    SELECT TOP (?) venditore, COUNT_BIG(*) AS numeroOrdini,
                           COALESCE(SUM(valoreTotale), 0) AS valoreTotale
                    FROM reporting.vw_ordini_vendita
                    GROUP BY venditore
                    ORDER BY valoreTotale DESC, venditore
                    """, true),
            new Sezione("PER_CLIENTE", """
                    SELECT TOP (?) cliente, COUNT_BIG(*) AS numeroOrdini,
                           COALESCE(SUM(valoreTotale), 0) AS valoreTotale
                    FROM reporting.vw_ordini_vendita
                    GROUP BY cliente
                    ORDER BY valoreTotale DESC, cliente
                    """, true),
            new Sezione("ULTIMI_DODICI_MESI", """
                    SELECT TOP (?) YEAR(dataRegistrazione) AS anno,
                           MONTH(dataRegistrazione) AS mese,
                           COUNT_BIG(*) AS numeroOrdini,
                           COALESCE(SUM(valoreTotale), 0) AS valoreTotale
                    FROM reporting.vw_ordini_vendita
                    WHERE dataRegistrazione >= DATEADD(
                        MONTH, -11, DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1))
                    GROUP BY YEAR(dataRegistrazione), MONTH(dataRegistrazione)
                    ORDER BY anno DESC, mese DESC
                    """, true),
            new Sezione("ORDINI_RECENTI", """
                    SELECT TOP (?) numeroOrdine, dataRegistrazione, dataRilascio,
                           dataAnnullamento, cliente, venditore, trasportatore,
                           stato, valoreTotale
                    FROM reporting.vw_ordini_vendita
                    ORDER BY dataRegistrazione DESC, idOrdine DESC
                    """, true));

    private final AiDatabaseConnectionProvider connections;
    private final AssistenteDatabaseProperties properties;

    JdbcContestoDatiOrdiniVendita(
            AiDatabaseConnectionProvider connections,
            AssistenteDatabaseProperties properties) {
        this.connections = connections;
        this.properties = properties;
    }

    @Override
    public String contenutoCorrente() {
        try (Connection connection = connections.getConnection()) {
            connection.setReadOnly(true);
            StringBuilder contesto = new StringBuilder()
                    .append("generatoAlle=").append(OffsetDateTime.now()).append('\n')
                    .append("fonte=").append(VIEW).append('\n');
            for (Sezione sezione : SEZIONI) {
                aggiungeSezione(connection, contesto, sezione);
            }
            return contesto.toString();
        } catch (SQLException ex) {
            throw new AssistenteNonDisponibileException(
                    "Impossibile consultare i dati correnti degli ordini di vendita", ex);
        }
    }

    private void aggiungeSezione(Connection connection, StringBuilder contesto, Sezione sezione)
            throws SQLException {
        contesto.append('[').append(sezione.nome()).append("]\n");
        try (PreparedStatement statement = connection.prepareStatement(sezione.sql())) {
            statement.setQueryTimeout(Math.max(1, properties.getQueryTimeoutSeconds()));
            if (sezione.limitata()) {
                statement.setInt(1, Math.max(1, properties.getMaximumRowsPerSection()));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<String> colonne = nomiColonne(resultSet.getMetaData());
                contesto.append(String.join("|", colonne)).append('\n');
                while (resultSet.next()) {
                    for (int indice = 1; indice <= colonne.size(); indice++) {
                        if (indice > 1) contesto.append('|');
                        contesto.append(sanifica(resultSet.getObject(indice)));
                    }
                    contesto.append('\n');
                }
            }
        }
    }

    private List<String> nomiColonne(ResultSetMetaData metadata) throws SQLException {
        List<String> colonne = new ArrayList<>();
        for (int indice = 1; indice <= metadata.getColumnCount(); indice++) {
            colonne.add(metadata.getColumnLabel(indice));
        }
        return colonne;
    }

    private String sanifica(Object valore) {
        if (valore == null) return "NULL";
        String testo = valore.toString()
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replace('|', '/');
        return testo.length() <= 200 ? testo : testo.substring(0, 200);
    }

    private record Sezione(String nome, String sql, boolean limitata) {
    }
}
