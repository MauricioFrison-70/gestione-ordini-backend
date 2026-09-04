package com.gestioneOrdini.infrastructure.ai;

import com.gestioneOrdini.application.assistente.exception.AssistenteNonDisponibileException;
import com.gestioneOrdini.infrastructure.config.AssistenteDatabaseProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JdbcContestoDatiOrdiniVenditaTest {
    @Mock AiDatabaseConnectionProvider connections;
    @Mock Connection connection;
    @Mock PreparedStatement statement;
    private AssistenteDatabaseProperties properties;

    @BeforeEach
    void setUp() {
        properties = new AssistenteDatabaseProperties();
        properties.setQueryTimeoutSeconds(7);
        properties.setMaximumRowsPerSection(15);
    }

    @Test
    void dovrebbeConsultareSoltantoLaViewAutorizzataConLimiti() throws Exception {
        when(connections.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        ResultSet riepilogo = risultatoSingolo("numeroOrdini", 12L);
        ResultSet stato = risultatoSingolo("stato", "RILASCIATO");
        ResultSet venditore = risultatoSingolo("venditore", "Mario Rossi");
        ResultSet cliente = risultatoSingolo("cliente", "Cliente Demo");
        ResultSet mese = risultatoSingolo("mese", 9);
        ResultSet ordine = risultatoSingolo("numeroOrdine", "OV-2026-000012");
        when(statement.executeQuery()).thenReturn(
                riepilogo, stato, venditore, cliente, mese, ordine);

        String contesto = new JdbcContestoDatiOrdiniVendita(connections, properties)
                .contenutoCorrente();

        assertThat(contesto)
                .contains("fonte=reporting.vw_ordini_vendita")
                .contains("[RIEPILOGO_GENERALE]")
                .contains("[PER_STATO]")
                .contains("[PER_VENDITORE]")
                .contains("[PER_CLIENTE]")
                .contains("[ULTIMI_DODICI_MESI]")
                .contains("[ORDINI_RECENTI]")
                .contains("OV-2026-000012");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(connection, times(6)).prepareStatement(sql.capture());
        assertThat(sql.getAllValues())
                .allSatisfy(query -> assertThat(query)
                        .contains("reporting.vw_ordini_vendita")
                        .doesNotContain("dbo."));
        verify(statement, times(6)).setQueryTimeout(7);
        verify(statement, times(4)).setInt(1, 15);
    }

    @Test
    void dovrebbeSegnalareIndisponibilitaSenzaEsporreDettagliJdbc() throws Exception {
        when(connections.getConnection()).thenThrow(new SQLException("password segreta"));

        assertThatThrownBy(() ->
                new JdbcContestoDatiOrdiniVendita(connections, properties).contenutoCorrente())
                .isInstanceOf(AssistenteNonDisponibileException.class)
                .hasMessage("Impossibile consultare i dati correnti degli ordini di vendita")
                .hasCauseInstanceOf(SQLException.class);
    }

    private ResultSet risultatoSingolo(String nomeColonna, Object valore) throws SQLException {
        ResultSet resultSet = org.mockito.Mockito.mock(ResultSet.class);
        ResultSetMetaData metadata = org.mockito.Mockito.mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(metadata);
        when(metadata.getColumnCount()).thenReturn(1);
        when(metadata.getColumnLabel(1)).thenReturn(nomeColonna);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getObject(1)).thenReturn(valore);
        return resultSet;
    }
}
