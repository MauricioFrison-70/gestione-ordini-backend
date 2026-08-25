package com.gestioneOrdini.presentation;

import com.gestioneOrdini.application.acquisto.dto.RigaOrdineAcquistoRequest;
import com.gestioneOrdini.application.acquisto.dto.RigaOrdineAcquistoResponse;
import com.gestioneOrdini.application.acquisto.usecase.CreateRigaOrdineAcquistoUseCase;
import com.gestioneOrdini.application.acquisto.usecase.DeleteRigaOrdineAcquistoUseCase;
import com.gestioneOrdini.application.acquisto.usecase.ListRigheOrdineAcquistoUseCase;
import com.gestioneOrdini.application.acquisto.usecase.UpdateRigaOrdineAcquistoUseCase;
import com.gestioneOrdini.infrastructure.persistence.mapper.acquisto.RigaOrdineAcquistoMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/ordini-acquisto/{ordineId}/righe")
public class RigaOrdineAcquistoController {
    private final CreateRigaOrdineAcquistoUseCase createUseCase;
    private final UpdateRigaOrdineAcquistoUseCase updateUseCase;
    private final ListRigheOrdineAcquistoUseCase listUseCase;
    private final DeleteRigaOrdineAcquistoUseCase deleteUseCase;
    private final RigaOrdineAcquistoMapper mapper;

    public RigaOrdineAcquistoController(
            CreateRigaOrdineAcquistoUseCase createUseCase,
            UpdateRigaOrdineAcquistoUseCase updateUseCase,
            ListRigheOrdineAcquistoUseCase listUseCase,
            DeleteRigaOrdineAcquistoUseCase deleteUseCase,
            RigaOrdineAcquistoMapper mapper) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.listUseCase = listUseCase;
        this.deleteUseCase = deleteUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<RigaOrdineAcquistoResponse> creare(
            @PathVariable Long ordineId,
            @Valid @RequestBody RigaOrdineAcquistoRequest request) {
        var creata = createUseCase.eseguire(ordineId, request);
        return ResponseEntity.created(URI.create(
                        "/api/ordini-acquisto/" + ordineId
                                + "/righe/" + creata.getId()))
                .body(mapper.toResponse(creata));
    }

    @GetMapping
    public ResponseEntity<List<RigaOrdineAcquistoResponse>> elencare(
            @PathVariable Long ordineId) {
        return ResponseEntity.ok(listUseCase.eseguire(ordineId).stream()
                .map(mapper::toResponse)
                .toList());
    }

    @PutMapping("/{rigaId}")
    public ResponseEntity<RigaOrdineAcquistoResponse> aggiornare(
            @PathVariable Long ordineId,
            @PathVariable Long rigaId,
            @Valid @RequestBody RigaOrdineAcquistoRequest request) {
        return ResponseEntity.ok(mapper.toResponse(
                updateUseCase.eseguire(ordineId, rigaId, request)));
    }

    @DeleteMapping("/{rigaId}")
    public ResponseEntity<Void> eliminare(
            @PathVariable Long ordineId, @PathVariable Long rigaId) {
        deleteUseCase.eseguire(ordineId, rigaId);
        return ResponseEntity.noContent().build();
    }
}
