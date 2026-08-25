package com.gestioneOrdini.presentation;

import com.gestioneOrdini.application.acquisto.dto.OrdineAcquistoRequest;
import com.gestioneOrdini.application.acquisto.dto.OrdineAcquistoResponse;
import com.gestioneOrdini.application.acquisto.usecase.AnnullaOrdineAcquistoUseCase;
import com.gestioneOrdini.application.acquisto.usecase.CreateOrdineAcquistoUseCase;
import com.gestioneOrdini.application.acquisto.usecase.DeleteOrdineAcquistoUseCase;
import com.gestioneOrdini.application.acquisto.usecase.GetOrdineAcquistoUseCase;
import com.gestioneOrdini.application.acquisto.usecase.ListOrdiniAcquistoUseCase;
import com.gestioneOrdini.application.acquisto.usecase.RiceviOrdineAcquistoUseCase;
import com.gestioneOrdini.infrastructure.persistence.mapper.acquisto.OrdineAcquistoMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/ordini-acquisto")
public class OrdineAcquistoController {
    private final CreateOrdineAcquistoUseCase createUseCase;
    private final GetOrdineAcquistoUseCase getUseCase;
    private final ListOrdiniAcquistoUseCase listUseCase;
    private final DeleteOrdineAcquistoUseCase deleteUseCase;
    private final RiceviOrdineAcquistoUseCase riceviUseCase;
    private final AnnullaOrdineAcquistoUseCase annullaUseCase;
    private final OrdineAcquistoMapper mapper;

    public OrdineAcquistoController(
            CreateOrdineAcquistoUseCase createUseCase,
            GetOrdineAcquistoUseCase getUseCase,
            ListOrdiniAcquistoUseCase listUseCase,
            DeleteOrdineAcquistoUseCase deleteUseCase,
            RiceviOrdineAcquistoUseCase riceviUseCase,
            AnnullaOrdineAcquistoUseCase annullaUseCase,
            OrdineAcquistoMapper mapper) {
        this.createUseCase = createUseCase;
        this.getUseCase = getUseCase;
        this.listUseCase = listUseCase;
        this.deleteUseCase = deleteUseCase;
        this.riceviUseCase = riceviUseCase;
        this.annullaUseCase = annullaUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<OrdineAcquistoResponse> creare(
            @Valid @RequestBody OrdineAcquistoRequest request) {
        var creato = createUseCase.eseguire(request);
        return ResponseEntity.created(URI.create(
                        "/api/ordini-acquisto/" + creato.getId()))
                .body(mapper.toResponse(creato));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdineAcquistoResponse> cercarePerId(
            @PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResponse(getUseCase.eseguire(id)));
    }

    @GetMapping
    public ResponseEntity<List<OrdineAcquistoResponse>> elencare() {
        return ResponseEntity.ok(listUseCase.eseguire().stream()
                .map(mapper::toResponse)
                .toList());
    }

    @PostMapping("/{id}/ricevere")
    public ResponseEntity<OrdineAcquistoResponse> ricevere(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResponse(riceviUseCase.eseguire(id)));
    }

    @PostMapping("/{id}/annullare")
    public ResponseEntity<OrdineAcquistoResponse> annullare(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResponse(annullaUseCase.eseguire(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminare(@PathVariable Long id) {
        deleteUseCase.eseguire(id);
        return ResponseEntity.noContent().build();
    }
}
