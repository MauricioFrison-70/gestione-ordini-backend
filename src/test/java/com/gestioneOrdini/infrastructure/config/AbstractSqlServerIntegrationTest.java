package com.gestioneOrdini.infrastructure.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;

@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractSqlServerIntegrationTest {

    private static final GenericContainer<?> MSSQL =
            new GenericContainer<>("mcr.microsoft.com/mssql/server:2022-latest")
                    .withEnv("ACCEPT_EULA", "Y")
                    .withEnv("SA_PASSWORD", "Password123!")
                    .withExposedPorts(1433)
                    .waitingFor(Wait.forLogMessage(
                            ".*SQL Server is now ready for client connections.*", 1))
                    .withStartupTimeout(Duration.ofMinutes(2));

    static {
        MSSQL.start();
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {

        java.util.function.Supplier<Object> url = () -> {
            String host = MSSQL.getHost();
            Integer port = MSSQL.getMappedPort(1433);
            return "jdbc:sqlserver://" + host + ":" + port + ";encrypt=false;trustServerCertificate=true";
        };

        registry.add("spring.datasource.url", url);
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "Password123!");
        registry.add("spring.datasource.driver-class-name",
                () -> "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        registry.add("reporting.datasource.url", url);
        registry.add("reporting.datasource.username", () -> "sa");
        registry.add("reporting.datasource.password", () -> "Password123!");
        registry.add("reporting.datasource.driver-class-name",
                () -> "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
    }
}
