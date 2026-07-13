package com.gestioneOrdini.presentation.controller;

import com.gestioneOrdini.application.dto.AgenteMapper;
import com.gestioneOrdini.application.dto.AgenteRequest;
import com.gestioneOrdini.application.dto.AgenteResponse;
import com.gestioneOrdini.application.usecase.*;
import com.gestioneOrdini.domain.model.Agente;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Controller REST responsabile della gestione delle operazioni sugli agenti.
 * <p>
 * Espone endpoint HTTP per creare, aggiornare, recuperare, elencare ed eliminare
 * agenti, delegando la logica applicativa ai rispettivi casi d'uso.
 * </p>
 *
 * <p><strong>Responsabilità principali:</strong></p>
 * <ul>
 *     <li>Ricevere e validare le richieste HTTP.</li>
 *     <li>Convertire DTO in oggetti di dominio tramite {@link AgenteMapper}.</li>
 *     <li>Delegare la logica ai casi d'uso dell'applicazione.</li>
 *     <li>Restituire risposte HTTP coerenti e tipizzate.</li>
 * </ul>
 *
 * <p>
 * Tutti gli endpoint sono esposti sotto il percorso <strong>/api/agenti</strong>.
 * </p>
 */

@RestController
@RequestMapping("/api/agenti")
public class AgenteController {

    private final CreateAgenteUseCase createUseCase;
    private final UpdateAgenteUseCase updateUseCase;
    private final DeleteAgenteUseCase deleteUseCase;
    private final GetAgenteUseCase getUseCase;
    private final ListAgentiUseCase listUseCase;
    private final AgenteMapper mapper;

    public AgenteController(CreateAgenteUseCase createUseCase,
                            UpdateAgenteUseCase updateUseCase,
                            DeleteAgenteUseCase deleteUseCase,
                            GetAgenteUseCase getUseCase,
                            ListAgentiUseCase listUseCase,
                            AgenteMapper mapper) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.deleteUseCase = deleteUseCase;
        this.getUseCase = getUseCase;
        this.listUseCase = listUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<AgenteResponse> criar(@RequestBody AgenteRequest request) {
        Agente agente = mapper.toDomain(request);
        Agente criado = createUseCase.executar(agente);

        URI location = URI.create("/api/agenti/" + criado.getId());

        return ResponseEntity.created(location)
                .body(mapper.toResponse(criado));

    }


    @GetMapping("/{id}")
    public ResponseEntity<AgenteResponse> buscarPorId(@PathVariable Long id) {
        Agente agente = getUseCase.executar(id);
        return ResponseEntity.ok(mapper.toResponse(agente));
    }

    @GetMapping
    public ResponseEntity<List<AgenteResponse>> listar() {
        List<AgenteResponse> lista = listUseCase.executar().stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AgenteResponse> atualizar(@PathVariable Long id,
                                                    @RequestBody AgenteRequest request) {
        Agente dados = mapper.toDomain(request);
        Agente atualizado = updateUseCase.executar(id, dados);
        return ResponseEntity.ok(mapper.toResponse(atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        deleteUseCase.executar(id);
        return ResponseEntity.noContent().build();
    }
}

