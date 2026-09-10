package com.gestioneOrdini.infrastructure.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;

@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractSqlServerIntegrationTest {

    private static final String USERNAME = "sa";
    private static final String PASSWORD = "Password123!";

    private static final GenericContainer<?> MSSQL =
            new GenericContainer<>("mcr.microsoft.com/mssql/server:2022-latest")
                    .withEnv("ACCEPT_EULA", "Y")
                    .withEnv("MSSQL_SA_PASSWORD", PASSWORD)
                    .withExposedPorts(1433)
                    .waitingFor(Wait.forLogMessage(
                            ".*SQL Server is now ready for client connections.*", 1))
                    .withStartupTimeout(Duration.ofMinutes(2));

    static {
        MSSQL.start();
        attendereConnessioneDatabase();
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {

        java.util.function.Supplier<Object> url = AbstractSqlServerIntegrationTest::jdbcUrl;

        registry.add("spring.datasource.url", url);
        registry.add("spring.datasource.username", () -> USERNAME);
        registry.add("spring.datasource.password", () -> PASSWORD);
        registry.add("spring.datasource.driver-class-name",
                () -> "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        registry.add("reporting.datasource.url", url);
        registry.add("reporting.datasource.username", () -> USERNAME);
        registry.add("reporting.datasource.password", () -> PASSWORD);
        registry.add("reporting.datasource.driver-class-name",
                () -> "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
    }

    private static String jdbcUrl() {
        return "jdbc:sqlserver://" + MSSQL.getHost() + ":" + MSSQL.getMappedPort(1433)
                + ";encrypt=false;trustServerCertificate=true";
    }

    private static void attendereConnessioneDatabase() {
        Instant scadenza = Instant.now().plus(Duration.ofMinutes(1));
        SQLException ultimoErrore = null;

        while (Instant.now().isBefore(scadenza)) {
            try (Connection ignored = DriverManager.getConnection(jdbcUrl(), USERNAME, PASSWORD)) {
                return;
            } catch (SQLException exception) {
                ultimoErrore = exception;
                attendereUnSecondo();
            }
        }

        throw new IllegalStateException(
                "SQL Server non ha accettato connessioni entro il tempo previsto",
                ultimoErrore
        );
    }

    private static void attendereUnSecondo() {
        try {
            Thread.sleep(1_000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Attesa di SQL Server interrotta", exception);
        }
    }
}
