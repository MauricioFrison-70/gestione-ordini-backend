package com.gestioneOrdini.infrastructure.reporting;

import com.gestioneOrdini.domain.reporting.model.ColonnaRapporto;
import com.gestioneOrdini.domain.reporting.model.ParametroRapporto;
import com.gestioneOrdini.domain.reporting.model.Rapporto;
import com.gestioneOrdini.domain.reporting.model.TipoParametroRapporto;
import com.gestioneOrdini.domain.reporting.repository.RapportoRepository;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcRapportoRepository implements RapportoRepository {

    private static final String SELECT_BASE = """
            SELECT id, codice, titolo, descrizione, nome_procedura, attivo
            FROM reporting.rapporti
            """;

    private final ReportingConnectionProvider connections;

    public JdbcRapportoRepository(ReportingConnectionProvider connections) {
        this.connections = connections;
    }

    @Override
    public List<Rapporto> findAllAttivi() {
        String sql = SELECT_BASE + " WHERE attivo = 1 ORDER BY ordine, titolo";
        try (Connection connection = connections.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Rapporto> rapporti = new ArrayList<>();
            while (resultSet.next()) {
                rapporti.add(mappaRapporto(connection, resultSet));
            }
            return rapporti;
        } catch (SQLException ex) {
            throw erroreCatalogo(ex);
        }
    }

    @Override
    public Optional<Rapporto> findById(Long id) {
        return cerca(SELECT_BASE + " WHERE id = ?", id);
    }

    @Override
    public Optional<Rapporto> findByCodice(String codice) {
        return cerca(SELECT_BASE + " WHERE codice = ?", codice);
    }

    private Optional<Rapporto> cerca(String sql, Object valore) {
        try (Connection connection = connections.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, valore);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) return Optional.empty();
                return Optional.of(mappaRapporto(connection, resultSet));
            }
        } catch (SQLException ex) {
            throw erroreCatalogo(ex);
        }
    }

    private Rapporto mappaRapporto(Connection connection, ResultSet resultSet) throws SQLException {
        long id = resultSet.getLong("id");
        return new Rapporto(
                id,
                resultSet.getString("codice"),
                resultSet.getString("titolo"),
                resultSet.getString("descrizione"),
                resultSet.getString("nome_procedura"),
                resultSet.getBoolean("attivo"),
                caricaParametri(connection, id),
                caricaColonne(connection, id));
    }

    private List<ParametroRapporto> caricaParametri(Connection connection, long rapportoId)
            throws SQLException {
        String sql = """
                SELECT id, nome_parametro, etichetta, tipo_sql, tipo_campo, obbligatorio,
                       ordine, valore_predefinito, procedura_opzioni
                FROM reporting.parametri_rapporti
                WHERE rapporto_id = ? AND attivo = 1
                ORDER BY ordine
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, rapportoId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<ParametroRapporto> parametri = new ArrayList<>();
                while (resultSet.next()) {
                    parametri.add(new ParametroRapporto(
                            resultSet.getLong("id"),
                            resultSet.getString("nome_parametro"),
                            resultSet.getString("etichetta"),
                            resultSet.getString("tipo_sql"),
                            TipoParametroRapporto.valueOf(resultSet.getString("tipo_campo")),
                            resultSet.getBoolean("obbligatorio"),
                            resultSet.getInt("ordine"),
                            resultSet.getString("valore_predefinito"),
                            resultSet.getString("procedura_opzioni")));
                }
                return parametri;
            }
        }
    }

    private List<ColonnaRapporto> caricaColonne(Connection connection, long rapportoId)
            throws SQLException {
        String sql = """
                SELECT nome_colonna, etichetta, formato, ordine, visibile, totalizzare
                FROM reporting.colonne_rapporti
                WHERE rapporto_id = ?
                ORDER BY ordine
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, rapportoId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<ColonnaRapporto> colonne = new ArrayList<>();
                while (resultSet.next()) {
                    colonne.add(new ColonnaRapporto(
                            resultSet.getString("nome_colonna"),
                            resultSet.getString("etichetta"),
                            resultSet.getString("formato"),
                            resultSet.getInt("ordine"),
                            resultSet.getBoolean("visibile"),
                            resultSet.getBoolean("totalizzare")));
                }
                return colonne;
            }
        }
    }

    private IllegalStateException erroreCatalogo(SQLException ex) {
        return new IllegalStateException(
                "Impossibile leggere il catalogo dei rapporti. Verificare l'installazione SQL.", ex);
    }
}
