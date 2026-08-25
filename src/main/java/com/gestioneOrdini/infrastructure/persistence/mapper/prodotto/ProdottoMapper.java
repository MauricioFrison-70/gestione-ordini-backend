package com.gestioneOrdini.infrastructure.persistence.mapper.prodotto;

import com.gestioneOrdini.application.prodotto.dto.ProdottoRequest;
import com.gestioneOrdini.application.prodotto.dto.ProdottoResponse;
import com.gestioneOrdini.application.prodotto.dto.ProdottoUpdateRequest;
import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import com.gestioneOrdini.infrastructure.persistence.entity.ProdottoEntity;
import org.springframework.stereotype.Component;

/**
 * DTO responsabile del trasferimento dei dati relativi a {@link Prodotto}
 * tra i vari livelli dell'applicazione.
 *
 * <p>Questa classe permette di esporre solo le informazioni necessarie
 * del modello di dominio, evitando un forte accoppiamento con l'entità
 * {@link Prodotto} e facilitando la serializzazione e deserializzazione
 * nelle operazioni di input e output.</p>
 *
 * <p>Il DTO può essere utilizzato per:</p>
 * <ul>
 *     <li>Ricevere i dati per la creazione o l'aggiornamento di un prodotto (input API)</li>
 *     <li>Restituire informazioni filtrate al client (output API)</li>
 *     <li>Mappare i dati tra il livello di servizio e quello di presentazione</li>
 * </ul>
 *
 * @author Mauricio
 * @version 1.0
 */

@Component
public class ProdottoMapper {

    public Prodotto toDomain(ProdottoRequest request) {
        return new Prodotto(
                request.codice(),
                request.descrizione(),
                request.valoreAcquisto(),
                request.valoreVendita(),
                0,
                request.scortaMinima(),
                request.archiviato()
        );
    }

    public Prodotto toDomain(ProdottoUpdateRequest request, Prodotto prodottoEsistente) {
        return new Prodotto(
                prodottoEsistente.getCodice(),
                request.descrizione(),
                request.valoreAcquisto(),
                request.valoreVendita(),
                prodottoEsistente.getQuantita(),
                request.scortaMinima(),
                request.archiviato()
        );
    }

    public ProdottoResponse toResponse(Prodotto prodotto) {
        return new ProdottoResponse(
                prodotto.getId(),
                prodotto.getCodice(),
                prodotto.getDescrizione(),
                prodotto.getValoreAcquisto(),
                prodotto.getValoreVendita(),
                prodotto.getQuantita(),
                prodotto.getScortaMinima(),
                prodotto.getArchiviato(),
                prodotto.getDataRegistrazione()
        );
    }

    public Prodotto toDomain(ProdottoEntity entity) {
        return new Prodotto(
                entity.getId(),
                entity.getCodice(),
                entity.getDescrizione(),
                entity.getValoreAcquisto(),
                entity.getValoreVendita(),
                entity.getQuantita(),
                entity.getScortaMinima(),
                entity.getArchiviato(),
                entity.getDataRegistrazione()
        );
    }

    public ProdottoEntity toEntity(Prodotto prodotto) {
        return new ProdottoEntity(
                prodotto.getId(),
                prodotto.getCodice(),
                prodotto.getDescrizione(),
                prodotto.getValoreAcquisto(),
                prodotto.getValoreVendita(),
                prodotto.getQuantita(),
                prodotto.getScortaMinima(),
                prodotto.getArchiviato(),
                prodotto.getDataRegistrazione()
        );
    }
}
