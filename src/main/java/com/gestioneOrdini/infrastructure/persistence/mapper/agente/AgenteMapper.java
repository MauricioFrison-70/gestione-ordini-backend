package com.gestioneOrdini.infrastructure.persistence.mapper.agente;

import com.gestioneOrdini.application.agente.dto.AgenteRequest;
import com.gestioneOrdini.application.agente.dto.AgenteResponse;
import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.infrastructure.persistence.entity.AgenteEntity;
import org.springframework.stereotype.Component;

/**
 * DTO responsabile del trasferimento dei dati relativi a {@link Agente}
 * tra i vari livelli dell'applicazione.
 *
 * <p>Questa classe permette di esporre solo le informazioni necessarie
 * del modello di dominio, evitando un forte accoppiamento con l'entità
 * {@link Agente} e facilitando la serializzazione e deserializzazione
 * nelle operazioni di input e output.</p>
 *
 * <p>Il DTO può essere utilizzato per:</p>
 * <ul>
 *     <li>Ricevere i dati per la creazione o l'aggiornamento di un agente (input API)</li>
 *     <li>Restituire informazioni filtrate al client (output API)</li>
 *     <li>Mappare i dati tra il livello di servizio e quello di presentazione</li>
 * </ul>
 *
 * @author Mauricio
 * @version 1.0
 */

@Component
public class AgenteMapper {

    public Agente toDomain(AgenteRequest request) {
        return new Agente(
                request.nome(),
                request.email(),
                request.tipoAgente(),
                request.archiviato()
        );
    }

    public AgenteResponse toResponse(Agente agente) {
        return new AgenteResponse(
                agente.getId(),
                agente.getNome(),
                agente.getEmail(),
                agente.getTipoAgente(),
                agente.getArchiviato(),
                agente.getDataRegistrazione()
        );
    }

    public Agente toDomain(AgenteEntity entity) {
        return new Agente(
                entity.getId(),
                entity.getNome(),
                entity.getEmail(),
                entity.getTipoAgente(),
                entity.getArchiviato(),
                entity.getDataRegistrazione()
        );
    }

    public AgenteEntity toEntity(Agente agente) {
        return new AgenteEntity(
                agente.getId(),
                agente.getNome(),
                agente.getEmail(),
                agente.getTipoAgente(),
                agente.getArchiviato(),
                agente.getDataRegistrazione()
        );
    }
}
