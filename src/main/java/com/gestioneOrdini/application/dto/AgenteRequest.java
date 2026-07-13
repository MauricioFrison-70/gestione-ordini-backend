package com.gestioneOrdini.application.dto;

import com.gestioneOrdini.domain.model.TipoAgente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AgenteRequest {

    @NotBlank(message = "Il nome è obbligatorio")
    @Size(max = 60, message = "Il nome può contenere al massimo 60 caratteri")
    private String nome;

    @NotBlank(message = "L'email è obbligatoria")
    @Email(message = "Email non valida")
    @Size(max = 80, message = "L'email può contenere al massimo 80 caratteri")
    private String email;

    @NotNull(message = "Il tipo di agente è obbligatorio")
    private TipoAgente tipoAgente;

    // Construtor vazio (necessário para Jackson)
    public AgenteRequest() {
    }

    // Construtor completo (ideal para testes e uso interno)
    public AgenteRequest(String nome, String email, TipoAgente tipoAgente) {
        this.nome = nome;
        this.email = email;
        this.tipoAgente= tipoAgente;
    }

    // Getters e setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public TipoAgente getTipoAgente() {
        return tipoAgente;
    }

    public void setTipoAgente(TipoAgente tipoAgente) {
        this.tipoAgente = tipoAgente;
    }
}
