package com.gestioneOrdini.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

    private final CorsProperties properties;

    public CorsConfig(CorsProperties properties) {
        this.properties = properties;
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration configurazione = new CorsConfiguration();
        configurazione.setAllowedOrigins(properties.allowedOrigins());
        configurazione.setAllowedMethods(List.of(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));
        configurazione.setAllowedHeaders(List.of("*"));
        configurazione.setExposedHeaders(List.of(HttpHeaders.LOCATION));
        configurazione.setAllowCredentials(false);
        configurazione.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource sorgente = new UrlBasedCorsConfigurationSource();
        sorgente.registerCorsConfiguration("/**", configurazione);
        return new CorsFilter(sorgente);
    }
}
