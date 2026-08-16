package com.gestioneOrdini.presentation;

import com.gestioneOrdini.domain.agente.model.TipoAgente;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tipo-agente")
public class TipoAgenteController {

    @GetMapping
    public TipoAgente[] listarTipos() {
        return TipoAgente.values();
    }
}

