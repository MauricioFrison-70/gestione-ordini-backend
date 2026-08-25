package com.gestioneOrdini.infrastructure.persistence.repository;

import com.gestioneOrdini.infrastructure.persistence.entity.ProdottoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;

/**
 * Repository JPA responsabile della gestione della persistenza per {@link ProdottoEntity}.
 * <p>
 * Questa interfaccia funge da adattatore verso il database, sfruttando le
 * funzionalità fornite da {@link JpaRepository} per operazioni CRUD standard.
 * Non contiene logica di business: il suo unico scopo è interagire con il
 * livello di persistenza in modo tipizzato e strutturato.
 * </p>
 *
 * <p><strong>Responsabilità:</strong></p>
 * <ul>
 *     <li>Fornire accesso alle operazioni CRUD per l'entità {@code ProdottoEntity}.</li>
 *     <li>Permettere l'estensione con query personalizzate, se necessario.</li>
 *     <li>Agire come componente di infrastruttura all'interno dell'architettura esagonale.</li>
 * </ul>
 *
 * <p>
 * Le implementazioni sono generate automaticamente da Spring Data JPA,
 * eliminando la necessità di codice boilerplate.
 * </p>
 */

public interface ProdottoJpaRepository extends JpaRepository<ProdottoEntity, Long> {
    Optional<ProdottoEntity> findByCodice(String codice);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProdottoEntity p where p.id = :id")
    Optional<ProdottoEntity> findByIdForUpdate(@Param("id") Long id);
}
