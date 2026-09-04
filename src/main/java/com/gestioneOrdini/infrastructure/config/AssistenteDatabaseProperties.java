package com.gestioneOrdini.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "assistente.database")
public class AssistenteDatabaseProperties {
    private boolean enabled;
    private String url;
    private String username;
    private String password;
    private String driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private int maximumPoolSize = 2;
    private long connectionTimeoutMs = 10_000;
    private int queryTimeoutSeconds = 10;
    private int maximumRowsPerSection = 20;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getDriverClassName() { return driverClassName; }
    public void setDriverClassName(String driverClassName) { this.driverClassName = driverClassName; }
    public int getMaximumPoolSize() { return maximumPoolSize; }
    public void setMaximumPoolSize(int maximumPoolSize) { this.maximumPoolSize = maximumPoolSize; }
    public long getConnectionTimeoutMs() { return connectionTimeoutMs; }
    public void setConnectionTimeoutMs(long connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }
    public int getQueryTimeoutSeconds() { return queryTimeoutSeconds; }
    public void setQueryTimeoutSeconds(int queryTimeoutSeconds) {
        this.queryTimeoutSeconds = queryTimeoutSeconds;
    }
    public int getMaximumRowsPerSection() { return maximumRowsPerSection; }
    public void setMaximumRowsPerSection(int maximumRowsPerSection) {
        this.maximumRowsPerSection = maximumRowsPerSection;
    }
}
