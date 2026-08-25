package com.gestioneOrdini.presentation;

import com.gestioneOrdini.application.ordine.dto.OrdineVenditaRequest;
import com.gestioneOrdini.application.ordine.dto.OrdineVenditaResponse;
import com.gestioneOrdini.application.ordine.usecase.*;
import com.gestioneOrdini.infrastructure.persistence.mapper.ordine.OrdineVenditaMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/ordini-vendita")
public class OrdineVenditaController {
    private final CreateOrdineVenditaUseCase createUseCase;
    private final GetOrdineVenditaUseCase getUseCase;
    private final ListOrdiniVenditaUseCase listUseCase;
    private final UpdateOrdineVenditaUseCase updateUseCase;
    private final DeleteOrdineVenditaUseCase deleteUseCase;
    private final RilasciaOrdineVenditaUseCase rilasciaUseCase;
    private final AnnullaOrdineVenditaUseCase annullaUseCase;
    private final OrdineVenditaMapper mapper;

    public OrdineVenditaController(CreateOrdineVenditaUseCase createUseCase,
                                    GetOrdineVenditaUseCase getUseCase,
                                    ListOrdiniVenditaUseCase listUseCase,
                                    UpdateOrdineVenditaUseCase updateUseCase,
                                    DeleteOrdineVenditaUseCase deleteUseCase,
                                    RilasciaOrdineVenditaUseCase rilasciaUseCase,
                                    AnnullaOrdineVenditaUseCase annullaUseCase,
                                    OrdineVenditaMapper mapper) {
        this.createUseCase = createUseCase;
        this.getUseCase = getUseCase;
        this.listUseCase = listUseCase;
        this.updateUseCase = updateUseCase;
        this.deleteUseCase = deleteUseCase;
        this.rilasciaUseCase = rilasciaUseCase;
        this.annullaUseCase = annullaUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<OrdineVenditaResponse> creare(@Valid @RequestBody OrdineVenditaRequest request) {
        var creato = createUseCase.eseguire(request);
        return ResponseEntity.created(URI.create("/api/ordini-vendita/" + creato.getId()))
                .body(mapper.toResponse(creato));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdineVenditaResponse> cercarePerId(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResponse(getUseCase.eseguire(id)));
    }

    @GetMapping
    public ResponseEntity<List<OrdineVenditaResponse>> elencare() {
        return ResponseEntity.ok(listUseCase.eseguire().stream().map(mapper::toResponse).toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrdineVenditaResponse> aggiornare(
            @PathVariable Long id, @Valid @RequestBody OrdineVenditaRequest request) {
        return ResponseEntity.ok(mapper.toResponse(updateUseCase.eseguire(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminare(@PathVariable Long id) {
        deleteUseCase.eseguire(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/rilasciare")
    public ResponseEntity<OrdineVenditaResponse> rilasciare(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResponse(rilasciaUseCase.eseguire(id)));
    }

    @PostMapping("/{id}/annullare")
    public ResponseEntity<OrdineVenditaResponse> annullare(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResponse(annullaUseCase.eseguire(id)));
    }
}
