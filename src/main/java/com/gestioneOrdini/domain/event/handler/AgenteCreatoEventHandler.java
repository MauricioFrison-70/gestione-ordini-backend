package com.gestioneOrdini.domain.event.handler;

import com.gestioneOrdini.domain.event.AgenteCreatoEvent;

public interface AgenteCreatoEventHandler {
    void handle(AgenteCreatoEvent event);
}