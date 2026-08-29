package com.gestioneOrdini.infrastructure.reporting;

import com.gestioneOrdini.infrastructure.config.ReportingDataSourceProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

@Component
@EnableConfigurationProperties(ReportingDataSourceProperties.class)
public class ReportingConnectionProvider {

    private final HikariDataSource dataSource;

    public ReportingConnectionProvider(ReportingDataSourceProperties properties) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getUrl());
        config.setUsername(properties.getUsername());
        config.setPassword(properties.getPassword());
        config.setDriverClassName(properties.getDriverClassName());
        config.setMaximumPoolSize(Math.max(1, properties.getMaximumPoolSize()));
        config.setMinimumIdle(0);
        config.setReadOnly(true);
        config.setConnectionTimeout(properties.getConnectionTimeoutMs());
        config.setPoolName("reporting-read-only");
        this.dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @PreDestroy
    void chiude() {
        dataSource.close();
    }
}
