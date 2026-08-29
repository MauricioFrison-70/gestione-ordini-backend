package com.gestioneOrdini.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "reporting.execution")
public class ReportingExecutionProperties {

    private int maximumRows = 1000;
    private int queryTimeoutSeconds = 30;

    public int getMaximumRows() { return maximumRows; }
    public void setMaximumRows(int maximumRows) { this.maximumRows = maximumRows; }
    public int getQueryTimeoutSeconds() { return queryTimeoutSeconds; }
    public void setQueryTimeoutSeconds(int queryTimeoutSeconds) {
        this.queryTimeoutSeconds = queryTimeoutSeconds;
    }
}
