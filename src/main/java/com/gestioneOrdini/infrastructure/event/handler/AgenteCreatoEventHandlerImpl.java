package com.gestioneOrdini.infrastructure.event.handler;

import com.gestioneOrdini.domain.agente.event.AgenteCreatoEvent;
import com.gestioneOrdini.domain.agente.event.handler.AgenteCreatoEventHandler;
import org.springframework.stereotype.Service;

@Service
public class AgenteCreatoEventHandlerImpl implements AgenteCreatoEventHandler {

    @Override
    public void handle(AgenteCreatoEvent event) {
        System.out.println("Evento: agente creato con ID " + event.getIdAgente());
        // lógica adicional...
    }
}

