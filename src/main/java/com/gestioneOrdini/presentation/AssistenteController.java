package com.gestioneOrdini.presentation;

import com.gestioneOrdini.application.assistente.dto.DomandaAssistenteRequest;
import com.gestioneOrdini.application.assistente.dto.RispostaAssistenteResponse;
import com.gestioneOrdini.application.assistente.usecase.RispondiDomandaAssistenteUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistente")
public class AssistenteController {
    private final RispondiDomandaAssistenteUseCase useCase;

    public AssistenteController(RispondiDomandaAssistenteUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/domande")
    public ResponseEntity<RispostaAssistenteResponse> rispondere(
            @Valid @RequestBody DomandaAssistenteRequest request) {
        return ResponseEntity.ok(useCase.eseguire(request));
    }
}
