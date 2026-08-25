package com.gestioneOrdini.presentation;

import com.gestioneOrdini.application.ordine.dto.RigaOrdineVenditaRequest;
import com.gestioneOrdini.application.ordine.dto.RigaOrdineVenditaResponse;
import com.gestioneOrdini.application.ordine.usecase.CreateRigaOrdineVenditaUseCase;
import com.gestioneOrdini.application.ordine.usecase.DeleteRigaOrdineVenditaUseCase;
import com.gestioneOrdini.application.ordine.usecase.GetRigaOrdineVenditaUseCase;
import com.gestioneOrdini.application.ordine.usecase.ListRigheOrdineVenditaUseCase;
import com.gestioneOrdini.application.ordine.usecase.UpdateRigaOrdineVenditaUseCase;
import com.gestioneOrdini.infrastructure.persistence.mapper.ordine.RigaOrdineVenditaMapper;
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
@RequestMapping("/api/ordini-vendita/{ordineId}/righe")
public class RigaOrdineVenditaController {
    private final CreateRigaOrdineVenditaUseCase createUseCase;
    private final UpdateRigaOrdineVenditaUseCase updateUseCase;
    private final GetRigaOrdineVenditaUseCase getUseCase;
    private final ListRigheOrdineVenditaUseCase listUseCase;
    private final DeleteRigaOrdineVenditaUseCase deleteUseCase;
    private final RigaOrdineVenditaMapper mapper;

    public RigaOrdineVenditaController(
            CreateRigaOrdineVenditaUseCase createUseCase,
            UpdateRigaOrdineVenditaUseCase updateUseCase,
            GetRigaOrdineVenditaUseCase getUseCase,
            ListRigheOrdineVenditaUseCase listUseCase,
            DeleteRigaOrdineVenditaUseCase deleteUseCase,
            RigaOrdineVenditaMapper mapper) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.getUseCase = getUseCase;
        this.listUseCase = listUseCase;
        this.deleteUseCase = deleteUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<RigaOrdineVenditaResponse> creare(
            @PathVariable Long ordineId,
            @Valid @RequestBody RigaOrdineVenditaRequest request) {
        var creata = createUseCase.eseguire(ordineId, request);
        return ResponseEntity.created(URI.create(
                        "/api/ordini-vendita/" + ordineId + "/righe/" + creata.getId()))
                .body(mapper.toResponse(creata));
    }

    @GetMapping("/{rigaId}")
    public ResponseEntity<RigaOrdineVenditaResponse> cercarePerId(
            @PathVariable Long ordineId, @PathVariable Long rigaId) {
        return ResponseEntity.ok(mapper.toResponse(
                getUseCase.eseguire(ordineId, rigaId)));
    }

    @GetMapping
    public ResponseEntity<List<RigaOrdineVenditaResponse>> elencare(
            @PathVariable Long ordineId) {
        return ResponseEntity.ok(listUseCase.eseguire(ordineId).stream()
                .map(mapper::toResponse)
                .toList());
    }

    @PutMapping("/{rigaId}")
    public ResponseEntity<RigaOrdineVenditaResponse> aggiornare(
            @PathVariable Long ordineId,
            @PathVariable Long rigaId,
            @Valid @RequestBody RigaOrdineVenditaRequest request) {
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
