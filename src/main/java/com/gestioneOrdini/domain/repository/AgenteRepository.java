package com.gestioneOrdini.domain.repository;

import com.gestioneOrdini.domain.model.Agente;
import com.gestioneOrdini.infrastructure.persistence.entity.AgenteEntity;

import java.util.List;
import java.util.Optional;


/**
 * Interfaccia che definisce le operazioni di accesso ai dati per l'entità {@link Agente}.
 * <p>
 * Rappresenta il contratto del livello di persistenza all'interno dell'architettura
 * esagonale (o DDD), consentendo alle componenti applicative di interagire con il
 * sistema di storage senza conoscerne i dettagli implementativi.
 * </p>
 *
 * <p><strong>Responsabilità principali:</strong></p>
 * <ul>
 *     <li>Salvare o aggiornare un agente.</li>
 *     <li>Recuperare un agente tramite il suo identificatore.</li>
 *     <li>Ottenere l'elenco completo degli agenti.</li>
 *     <li>Eliminare un agente tramite ID.</li>
 * </ul>
 *
 * <p>
 * Le implementazioni concrete di questa interfaccia possono utilizzare database
 * relazionali, NoSQL, memoria volatile o qualsiasi altro meccanismo di persistenza.
 * </p>
 */

public interface AgenteRepository {
    List<AgenteEntity> findAll();
    Optional<AgenteEntity> findById(Long id);
    AgenteEntity salva(AgenteEntity agente);
    void deleteById(Long id);
}

