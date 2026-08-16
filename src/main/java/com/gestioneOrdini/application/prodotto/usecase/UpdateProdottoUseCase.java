package com.gestioneOrdini.application.prodotto.usecase;

import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import com.gestioneOrdini.domain.prodotto.repository.ProdottoRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Caso d'uso responsabile dell'aggiornamento dei dati di un prodotto esistente.
 * <p>
 * Mantiene la logica di modifica nel livello applicativo, delegando al dominio
 * la gestione dei dati e garantendo una chiara separazione delle responsabilità.
 * </p>
 */
@Service
public class UpdateProdottoUseCase {

    private final ProdottoRepository repository;

    public UpdateProdottoUseCase(ProdottoRepository repository) {
        this.repository = repository;
    }

    /**
     * Aggiorna i dati dell prodotto identificato da {@code id}.
     *
     * @param id             identificatore dell prodotto da aggiornare
     * @param datiAggiornati modello di dominio contenente i nuovi valori
     * @return modello di dominio aggiornato
     * @throws EntityNotFoundException se il prodotto non esiste
     */
    public Prodotto eseguire(Long id, Prodotto datiAggiornati) {

        Prodotto esistente = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Prodotto", id));

        esistente.setDescrizione(datiAggiornati.getDescrizione());
        esistente.setValoreAcquisto(datiAggiornati.getValoreAcquisto());
        esistente.setValoreVendita(datiAggiornati.getValoreVendita());
        esistente.setQuantita(datiAggiornati.getQuantita());
        esistente.setScortaMinima(datiAggiornati.getScortaMinima());
        esistente.setArchiviato(datiAggiornati.getArchiviato());
        return repository.save(esistente);
    }
}
