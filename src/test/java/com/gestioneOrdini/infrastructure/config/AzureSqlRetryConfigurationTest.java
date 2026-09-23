package com.gestioneOrdini.infrastructure.config;

import com.zaxxer.hikari.HikariConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.env.MockEnvironment;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AzureSqlRetryConfigurationTest {

    @Test
    void configuraRetryDiConnessionePerTuttiIDataSourceAzure() throws IOException {
        var environment = new MockEnvironment();
        var source = new YamlPropertySourceLoader()
                .load("azure", new ClassPathResource("application-azure.yaml"))
                .get(0);
        environment.getPropertySources().addFirst(source);
        var binder = Binder.get(environment);

        var primary = new HikariConfig();
        binder.bind("spring.datasource.hikari", Bindable.ofInstance(primary));
        assertEquals(12_000, primary.getConnectionTimeout());
        assertEquals(-1, primary.getInitializationFailTimeout());
        assertEquals("10", primary.getDataSourceProperties().getProperty("loginTimeout"));
        assertEquals("1", primary.getDataSourceProperties().getProperty("connectRetryCount"));
        assertEquals("5", primary.getDataSourceProperties().getProperty("connectRetryInterval"));

        var reporting = binder.bind("reporting.datasource", ReportingDataSourceProperties.class).get();
        assertEquals(12_000, reporting.getConnectionTimeoutMs());
        assertEquals(10, reporting.getLoginTimeoutSeconds());
        assertEquals(1, reporting.getConnectRetryCount());
        assertEquals(5, reporting.getConnectRetryIntervalSeconds());

        var assistant = binder.bind("assistente.database", AssistenteDatabaseProperties.class).get();
        assertEquals(12_000, assistant.getConnectionTimeoutMs());
        assertEquals(10, assistant.getLoginTimeoutSeconds());
        assertEquals(1, assistant.getConnectRetryCount());
        assertEquals(5, assistant.getConnectRetryIntervalSeconds());
    }
}
