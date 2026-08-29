package com.gestioneOrdini.presentation;

import com.gestioneOrdini.application.reporting.dto.EseguiRapportoRequest;
import com.gestioneOrdini.application.reporting.dto.EsecuzioneRapportoResponse;
import com.gestioneOrdini.application.reporting.dto.ParametroRapportoResponse;
import com.gestioneOrdini.application.reporting.dto.OpzioneParametroResponse;
import com.gestioneOrdini.application.reporting.dto.RapportoResponse;
import com.gestioneOrdini.application.reporting.usecase.EseguiRapportoUseCase;
import com.gestioneOrdini.application.reporting.usecase.GetRapportoUseCase;
import com.gestioneOrdini.application.reporting.usecase.ListRapportiUseCase;
import com.gestioneOrdini.application.reporting.usecase.ListOpzioniParametroUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rapporti")
public class RapportoController {

    private final ListRapportiUseCase listUseCase;
    private final GetRapportoUseCase getUseCase;
    private final EseguiRapportoUseCase eseguiUseCase;
    private final ListOpzioniParametroUseCase listOpzioniUseCase;

    public RapportoController(ListRapportiUseCase listUseCase,
                              GetRapportoUseCase getUseCase,
                              EseguiRapportoUseCase eseguiUseCase,
                              ListOpzioniParametroUseCase listOpzioniUseCase) {
        this.listUseCase = listUseCase;
        this.getUseCase = getUseCase;
        this.eseguiUseCase = eseguiUseCase;
        this.listOpzioniUseCase = listOpzioniUseCase;
    }

    @GetMapping
    public ResponseEntity<List<RapportoResponse>> elencare() {
        return ResponseEntity.ok(listUseCase.eseguire().stream().map(RapportoResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RapportoResponse> cercarePerId(@PathVariable Long id) {
        return ResponseEntity.ok(RapportoResponse.from(getUseCase.eseguire(id)));
    }

    @GetMapping("/{id}/parametri")
    public ResponseEntity<List<ParametroRapportoResponse>> elencareParametri(@PathVariable Long id) {
        return ResponseEntity.ok(getUseCase.eseguire(id).getParametri().stream()
                .map(ParametroRapportoResponse::from).toList());
    }

    @GetMapping("/{id}/parametri/{nome}/opzioni")
    public ResponseEntity<List<OpzioneParametroResponse>> elencareOpzioni(
            @PathVariable Long id,
            @PathVariable String nome) {
        return ResponseEntity.ok(listOpzioniUseCase.eseguire(id, nome));
    }

    @PostMapping("/{id}/esegui")
    public ResponseEntity<EsecuzioneRapportoResponse> eseguire(
            @PathVariable Long id,
            @Valid @RequestBody EseguiRapportoRequest request) {
        return ResponseEntity.ok(eseguiUseCase.eseguire(id, request.parametri()));
    }
}
