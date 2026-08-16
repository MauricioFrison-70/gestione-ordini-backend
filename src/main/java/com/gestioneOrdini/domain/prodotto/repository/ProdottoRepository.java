package com.gestioneOrdini.domain.prodotto.repository;

import com.gestioneOrdini.domain.prodotto.model.Prodotto;

import java.util.List;
import java.util.Optional;


/**
 * Interfaccia che definisce le operazioni di accesso ai dati per l'entità {@link Prodotto}.
 * <p>
 * Rappresenta il contratto del livello di persistenza all'interno dell'architettura
 * esagonale (o DDD), consentendo alle componenti applicative di interagire con il
 * sistema di storage senza conoscerne i dettagli implementativi.
 * </p>
 *
 * <p><strong>Responsabilità principali:</strong></p>
 * <ul>
 *     <li>Salvare o aggiornare un prodotto.</li>
 *     <li>Recuperare un prodotto tramite il suo identificatore.</li>
 *     <li>Ottenere l'elenco completo degli prodotti.</li>
 *     <li>Eliminare un prodotti tramite ID.</li>
 * </ul>
 *
 * <p>
 * Le implementazioni concrete di questa interfaccia possono utilizzare database
 * relazionali, NoSQL, memoria volatile o qualsiasi altro meccanismo di persistenza.
 * </p>
 */

public interface ProdottoRepository {
    List<Prodotto> findAll();

    Optional<Prodotto> findById(Long id);

    Prodotto save(Prodotto prodotto);

    void deleteById(Long id);

    boolean existsById(Long id);
}
