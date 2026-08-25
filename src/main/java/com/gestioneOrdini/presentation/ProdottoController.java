package com.gestioneOrdini.presentation;

import com.gestioneOrdini.application.prodotto.dto.ProdottoRequest;
import com.gestioneOrdini.application.prodotto.dto.ProdottoResponse;
import com.gestioneOrdini.application.prodotto.dto.ProdottoUpdateRequest;
import com.gestioneOrdini.application.prodotto.usecase.*;
import com.gestioneOrdini.infrastructure.persistence.mapper.prodotto.ProdottoMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Controller REST responsabile della gestione delle operazioni sui prodotti.
 * <p>
 * Espone endpoint HTTP per creare, aggiornare, recuperare, elencare ed eliminare
 * prodotti, delegando la logica applicativa ai rispettivi casi d'uso e gestendo
 * la conversione tra DTO e modelli di dominio.
 * </p>
 */
@RestController
@RequestMapping("/api/prodotti")
public class ProdottoController {

    private final CreateProdottoUseCase createUseCase;
    private final UpdateProdottoUseCase updateUseCase;
    private final DeleteProdottoUseCase deleteUseCase;
    private final GetProdottoUseCase getUseCase;
    private final ListProdottiUseCase listUseCase;
    private final ProdottoMapper mapper;

    public ProdottoController(CreateProdottoUseCase createUseCase,
                              UpdateProdottoUseCase updateUseCase,
                              DeleteProdottoUseCase deleteUseCase,
                              GetProdottoUseCase getUseCase,
                              ListProdottiUseCase listUseCase,
                              ProdottoMapper mapper) {

        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.deleteUseCase = deleteUseCase;
        this.getUseCase = getUseCase;
        this.listUseCase = listUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<ProdottoResponse> creare(@Valid @RequestBody ProdottoRequest request) {
        var prodotto = mapper.toDomain(request);
        var creato = createUseCase.eseguire(prodotto);

        URI location = URI.create("/api/prodotti/" + creato.getId());

        return ResponseEntity.created(location)
                .body(mapper.toResponse(creato));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdottoResponse> cercarePerId(@PathVariable Long id) {
        var prodotto = getUseCase.eseguire(id);
        return ResponseEntity.ok(mapper.toResponse(prodotto));
    }

    @GetMapping
    public ResponseEntity<List<ProdottoResponse>> elencare() {
        var lista = listUseCase.eseguire().stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdottoResponse> aggiornare(@PathVariable Long id,
                                                       @Valid @RequestBody ProdottoUpdateRequest request) {
        var prodottoEsistente = getUseCase.eseguire(id);
        var dati = mapper.toDomain(request, prodottoEsistente);
        var aggiornato = updateUseCase.eseguire(id, dati);
        return ResponseEntity.ok(mapper.toResponse(aggiornato));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminare(@PathVariable Long id) {
        deleteUseCase.eseguire(id);
        return ResponseEntity.noContent().build();
    }
}
