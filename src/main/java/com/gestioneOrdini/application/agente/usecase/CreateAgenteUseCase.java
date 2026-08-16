package com.gestioneOrdini.application.agente.usecase;

import com.gestioneOrdini.domain.agente.event.AgenteCreatoEvent;
import com.gestioneOrdini.domain.agente.event.handler.AgenteCreatoEventHandler;
import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.repository.AgenteRepository;
import org.springframework.stereotype.Service;

/**
 * Caso d'uso responsabile della creazione di un nuovo {@link Agente}
 * all'interno del sistema.
 *
 * <p>Incapsula la logica applicativa necessaria per registrare un nuovo
 * agente, delegando la persistenza al {@link AgenteRepository}. Dopo la
 * creazione viene generato un evento di dominio {@link AgenteCreatoEvent}
 * per notificare altri componenti interessati.</p>
 *
 * @author Mauricio
 * @version 2.0
 */
@Service
public class CreateAgenteUseCase {

    private final AgenteRepository repository;
    private final AgenteCreatoEventHandler eventHandler;

    public CreateAgenteUseCase(AgenteRepository repository,
                               AgenteCreatoEventHandler eventHandler) {
        this.repository = repository;
        this.eventHandler = eventHandler;
    }

    /**
     * Esegue la creazione di un nuovo agente e genera un evento di dominio.
     *
     * @param agente modello di dominio contenente i dati dell'agente
     * @return l'entità persistita
     */
    public Agente eseguire(Agente agente) {

        // Persistência delegada ao repositório do domínio
        Agente salvato = repository.save(agente);

        // Evento de domínio
        var event = new AgenteCreatoEvent(
                salvato.getId(),
                salvato.getEmail(),
                salvato.getNome(),
                salvato.getTipoAgente()
        );

        eventHandler.handle(event);

        return salvato;
    }
}
