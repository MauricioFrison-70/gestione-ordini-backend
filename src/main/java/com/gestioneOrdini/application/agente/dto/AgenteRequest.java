package com.gestioneOrdini.application.agente.dto;

import com.gestioneOrdini.domain.agente.model.TipoAgente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Rappresenta i dati necessari per creare o aggiornare un agente.
 *
 * <p>Questa classe funge da DTO di input per le operazioni REST e
 * definisce i vincoli di validazione relativi ai campi obbligatori.</p>
 *
 * <p>Il campo {@code archiviato} è opzionale: se non specificato,
 * viene automaticamente impostato a {@code false}.</p>
 */

public record AgenteRequest(

        @NotBlank(message = "Il nome è obbligatorio")
        @Size(max = 60, message = "Il nome può contenere al massimo 60 caratteri")
        String nome,

        @NotBlank(message = "L'email è obbligatoria")
        @Email(message = "Email non valida")
        @Size(max = 80, message = "L'email può contenere al massimo 80 caratteri")
        String email,

        @NotNull(message = "Il tipo di agente è obbligatorio")
        TipoAgente tipoAgente,

        Boolean archiviato
) {
    public AgenteRequest {
        if (archiviato == null) {
            archiviato = false;
        }
    }
}
