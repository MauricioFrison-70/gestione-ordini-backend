package com.gestioneOrdini.infrastructure.ai;

import com.gestioneOrdini.infrastructure.config.AssistenteDatabaseProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

@Component
@ConditionalOnProperty(prefix = "assistente.database", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(AssistenteDatabaseProperties.class)
class HikariAiDatabaseConnectionProvider implements AiDatabaseConnectionProvider {
    private final HikariDataSource dataSource;

    HikariAiDatabaseConnectionProvider(AssistenteDatabaseProperties properties) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getUrl());
        config.setUsername(properties.getUsername());
        config.setPassword(properties.getPassword());
        config.setDriverClassName(properties.getDriverClassName());
        config.setMaximumPoolSize(Math.max(1, properties.getMaximumPoolSize()));
        config.setMinimumIdle(0);
        config.setReadOnly(true);
        config.setConnectionTimeout(Math.max(1_000, properties.getConnectionTimeoutMs()));
        config.setPoolName("assistente-ai-read-only");
        this.dataSource = new HikariDataSource(config);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @PreDestroy
    void chiude() {
        dataSource.close();
    }
}
