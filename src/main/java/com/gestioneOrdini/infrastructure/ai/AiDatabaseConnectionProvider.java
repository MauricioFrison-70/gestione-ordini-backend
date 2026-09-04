package com.gestioneOrdini.infrastructure.ai;

import java.sql.Connection;
import java.sql.SQLException;

interface AiDatabaseConnectionProvider {
    Connection getConnection() throws SQLException;
}
