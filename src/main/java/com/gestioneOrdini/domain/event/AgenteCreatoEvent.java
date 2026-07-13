package com.gestioneOrdini.domain.event;

import com.gestioneOrdini.domain.model.TipoAgente;

/**
 * Evento di dominio che rappresenta la creazione di un nuovo agente.
 */
public class AgenteCreatoEvent {

    private final Long idAgente;
    private final String email;
    private final String nome;
    private final TipoAgente tipoAgente;

    public AgenteCreatoEvent(Long idAgente, String email, String  nome, TipoAgente tipoAgente) {
        this.idAgente = idAgente;
        this.email = email;
        this.nome = nome;
        this.tipoAgente = tipoAgente;
    }

    public Long getIdAgente() {
        return idAgente;
    }

    public String getEmail() {
        return email;
    }

    public String getNome() {
        return nome;
    }

    public TipoAgente getTipoAgente() {
        return tipoAgente;
    }

}
