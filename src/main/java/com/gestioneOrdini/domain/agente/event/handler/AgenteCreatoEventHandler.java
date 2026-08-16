package com.gestioneOrdini.domain.agente.event.handler;

import com.gestioneOrdini.domain.agente.event.AgenteCreatoEvent;

public interface AgenteCreatoEventHandler {
    void handle(AgenteCreatoEvent event);
}