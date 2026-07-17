package com.gestioneOrdini.application.usecase;

import com.gestioneOrdini.domain.model.Agente;
import com.gestioneOrdini.domain.repository.AgenteRepository;
import com.gestioneOrdini.domain.event.AgenteCreatoEvent;
import com.gestioneOrdini.domain.event.handler.AgenteCreatoEventHandler;
import com.gestioneOrdini.infrastructure.persistence.entity.AgenteEntity;
import org.springframework.stereotype.Service;

/**
 * Caso d'uso responsabile della creazione di un nuovo {@link Agente}
 * all'interno del sistema.
 *
 * <p>Questa classe incapsula la logica applicativa necessaria per
 * registrare un nuovo agente, delegando la persistenza al
 * {@link AgenteRepository}. Inoltre, dopo la creazione, viene generato
 * un evento di dominio ({@link AgenteCreatoEvent}) per notificare altri
 * componenti interessati.</p>
 *
 * <p>Il caso d'uso può essere invocato da controller, servizi di
 * orchestrazione o altri componenti dell'applicazione che necessitano
 * di creare un agente in modo controllato.</p>
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
     * @param agente oggetto di dominio contenente i dati dell'agente da creare
     * @return l'agente creato e persistito
     */


    public AgenteEntity executar(Agente agente) {

        AgenteEntity entity = new AgenteEntity();
        entity.setNome(agente.getNome());
        entity.setEmail(agente.getEmail());
        entity.setTipoAgente(agente.getTipoAgente());
        entity.setArchiviato(agente.getArchiviato());

        AgenteEntity salvato = repository.salva(entity);

        AgenteCreatoEvent event = new AgenteCreatoEvent(
                salvato.getId(),
                salvato.getEmail(),
                salvato.getNome(),
                salvato.getTipoAgente()
        );
        eventHandler.handle(event);

        return salvato;
    }

}
