package com.gestioneOrdini.infrastructure.ai;

import com.gestioneOrdini.application.assistente.port.ContestoDatiOrdiniVendita;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "assistente.database",
        name = "enabled",
        havingValue = "false",
        matchIfMissing = true)
class ContestoDatiOrdiniVenditaDisabilitato implements ContestoDatiOrdiniVendita {
    @Override
    public String contenutoCorrente() {
        return "La consultazione dei dati correnti è disabilitata.";
    }
}
