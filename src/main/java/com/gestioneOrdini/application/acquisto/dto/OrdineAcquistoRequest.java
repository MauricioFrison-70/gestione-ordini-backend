package com.gestioneOrdini.application.acquisto.dto;

import jakarta.validation.constraints.NotNull;

public record OrdineAcquistoRequest(
        @NotNull(message = "Il fornitore è obbligatorio")
        Long fornitoreId
) {}
