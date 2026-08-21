package com.gestioneOrdini.presentation;

import com.gestioneOrdini.application.agente.dto.AgenteRequest;
import com.gestioneOrdini.application.agente.dto.AgenteResponse;
import com.gestioneOrdini.application.agente.dto.AgenteUtilizzoResponse;
import com.gestioneOrdini.application.agente.usecase.*;
import com.gestioneOrdini.infrastructure.persistence.mapper.agente.AgenteMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Controller REST responsabile della gestione delle operazioni sugli agenti.
 * <p>
 * Espone endpoint HTTP per creare, aggiornare, recuperare, elencare ed eliminare
 * agenti, delegando la logica applicativa ai rispettivi casi d'uso e gestendo
 * la conversione tra DTO e modelli di dominio.
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
    private final CheckAgenteUtilizzatoUseCase checkUtilizzatoUseCase;

    public AgenteController(CreateAgenteUseCase createUseCase,
                            UpdateAgenteUseCase updateUseCase,
                            DeleteAgenteUseCase deleteUseCase,
                            GetAgenteUseCase getUseCase,
                            ListAgentiUseCase listUseCase,
                            AgenteMapper mapper,
                            CheckAgenteUtilizzatoUseCase checkUtilizzatoUseCase) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.deleteUseCase = deleteUseCase;
        this.getUseCase = getUseCase;
        this.listUseCase = listUseCase;
        this.mapper = mapper;
        this.checkUtilizzatoUseCase = checkUtilizzatoUseCase;
    }

    @PostMapping
    public ResponseEntity<AgenteResponse> creare(@Valid @RequestBody AgenteRequest request) {
        var agente = mapper.toDomain(request);
        var creato = createUseCase.eseguire(agente);

        URI location = URI.create("/api/agenti/" + creato.getId());

        return ResponseEntity.created(location)
                .body(mapper.toResponse(creato));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgenteResponse> cercarePerId(@PathVariable Long id) {
        var agente = getUseCase.eseguire(id);
        return ResponseEntity.ok(mapper.toResponse(agente));
    }

    @GetMapping
    public ResponseEntity<List<AgenteResponse>> elencare() {
        var lista = listUseCase.eseguire().stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AgenteResponse> aggiornare(@PathVariable Long id,
                                                     @Valid @RequestBody AgenteRequest request) {
        var dati = mapper.toDomain(request);
        var aggiornato = updateUseCase.eseguire(id, dati);
        return ResponseEntity.ok(mapper.toResponse(aggiornato));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminare(@PathVariable Long id) {
        deleteUseCase.eseguire(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/utilizzo-ordini")
    public ResponseEntity<AgenteUtilizzoResponse> verificareUtilizzo(@PathVariable Long id) {
        return ResponseEntity.ok(new AgenteUtilizzoResponse(checkUtilizzatoUseCase.eseguire(id)));
    }
}
