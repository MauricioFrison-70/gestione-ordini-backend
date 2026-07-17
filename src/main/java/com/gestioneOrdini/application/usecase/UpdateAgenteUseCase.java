package com.gestioneOrdini.application.usecase;

import com.gestioneOrdini.domain.model.Agente;
import com.gestioneOrdini.domain.repository.AgenteRepository;
import com.gestioneOrdini.infrastructure.persistence.entity.AgenteEntity;
import org.springframework.stereotype.Service;

/**
 * Caso d'uso responsabile dell'aggiornamento dei dati di un agente esistente.
 * <p>
 * Questa classe centralizza la logica di modifica, garantendo che
 * l'operazione venga eseguita in modo coerente con le regole di business
 * e delegando al {@link AgenteRepository} la persistenza delle modifiche.
 * </p>
 *
 * <p><strong>Responsabilità:</strong></p>
 * <ul>
 *     <li>Recuperare l'agente esistente tramite il suo identificatore.</li>
 *     <li>Aggiornare i campi modificabili con i nuovi valori forniti.</li>
 *     <li>Salvare l'entità aggiornata nel repository.</li>
 *     <li>Generare un'eccezione significativa se l'agente non esiste.</li>
 * </ul>
 *
 * <p>
 * Questo caso d'uso contribuisce alla separazione delle responsabilità,
 * mantenendo la logica di aggiornamento isolata e facilmente testabile.
 * </p>
 */

@Service
public class UpdateAgenteUseCase {

    private final AgenteRepository repository;

    public UpdateAgenteUseCase(AgenteRepository repository) {
        this.repository = repository;
    }

    public AgenteEntity executar(Long id, Agente dadosAtualizados) {
        AgenteEntity existente = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Agente non trovato"));

        existente.setNome(dadosAtualizados.getNome());
        existente.setEmail(dadosAtualizados.getEmail());
        existente.setTipoAgente(dadosAtualizados.getTipoAgente());
        existente.setArchiviato(dadosAtualizados.getArchiviato());

        return repository.salva(existente);
    }
}
