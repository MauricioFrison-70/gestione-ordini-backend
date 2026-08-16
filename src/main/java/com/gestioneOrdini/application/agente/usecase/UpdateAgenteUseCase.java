package com.gestioneOrdini.application.agente.usecase;

import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.repository.AgenteRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Caso d'uso responsabile dell'aggiornamento dei dati di un agente esistente.
 * <p>
 * Mantiene la logica di modifica nel livello applicativo, delegando al dominio
 * la gestione dei dati e garantendo una chiara separazione delle responsabilità.
 * </p>
 */
@Service
public class UpdateAgenteUseCase {

    private final AgenteRepository repository;

    public UpdateAgenteUseCase(AgenteRepository repository) {
        this.repository = repository;
    }

    /**
     * Aggiorna i dati dell'agente identificato da {@code id}.
     *
     * @param id             identificatore dell'agente da aggiornare
     * @param datiAggiornati modello di dominio contenente i nuovi valori
     * @return modello di dominio aggiornato
     * @throws EntityNotFoundException se l'agente non esiste
     */
    public Agente eseguire(Long id, Agente datiAggiornati) {

        Agente esistente = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agente", id));

        esistente.setNome(datiAggiornati.getNome());
        esistente.setEmail(datiAggiornati.getEmail());
        esistente.setTipoAgente(datiAggiornati.getTipoAgente());
        esistente.setArchiviato(datiAggiornati.getArchiviato());

        return repository.save(esistente);
    }
}
