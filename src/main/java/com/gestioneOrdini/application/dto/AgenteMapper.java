package com.gestioneOrdini.application.dto;

import com.gestioneOrdini.domain.model.Agente;
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
    /**
     * Converte o DTO de entrada (API) para o modelo de domínio.
     */
    public Agente toDomain(AgenteRequest request) {
        return new Agente(
                request.getNome(),
                request.getEmail(),
                request.getTipoAgente(),
                request.getArchiviato()
        );
    }

    /**
     * Converte a entidade persistida para o DTO de saída (API).
     */
    public AgenteResponse toResponse(AgenteEntity entity) {
        return new AgenteResponse(
                entity.getId(),
                entity.getNome(),
                entity.getEmail(),
                entity.getTipoAgente(),
                entity.getArchiviato(),
                entity.getDataRegistrazione()
        );
    }
}

