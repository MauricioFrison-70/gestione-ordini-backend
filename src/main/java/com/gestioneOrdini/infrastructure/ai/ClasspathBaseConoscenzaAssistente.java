package com.gestioneOrdini.infrastructure.ai;

import com.gestioneOrdini.application.assistente.port.BaseConoscenzaAssistente;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class ClasspathBaseConoscenzaAssistente implements BaseConoscenzaAssistente {
    private static final String RISORSA = "ai/base-conoscenza-sistema.md";
    private final String contenuto;

    public ClasspathBaseConoscenzaAssistente() {
        try (var input = new ClassPathResource(RISORSA).getInputStream()) {
            contenuto = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Base di conoscenza IA non disponibile", ex);
        }
    }

    @Override
    public String contenuto() {
        return contenuto;
    }
}
