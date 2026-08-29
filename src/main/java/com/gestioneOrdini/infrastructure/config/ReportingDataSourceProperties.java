package com.gestioneOrdini.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "reporting.datasource")
public class ReportingDataSourceProperties {

    private String url;
    private String username;
    private String password;
    private String driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private int maximumPoolSize = 3;
    private long connectionTimeoutMs = 10_000;

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
}
