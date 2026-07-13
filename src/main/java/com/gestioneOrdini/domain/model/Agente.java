package com.gestioneOrdini.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Rappresenta un agente all'interno del dominio dell'applicazione.
 * <p>
 * Questa classe modella l'entità principale utilizzata per gestire
 * informazioni relative agli agenti, come identificazione, dati personali
 * e tipologia. È una semplice struttura di dominio (POJO) utilizzata
 * dalle varie componenti dell'applicazione.
 * </p>
 *
 * <p><strong>Campi principali:</strong></p>
 * <ul>
 *     <li><strong>id</strong>: identificatore univoco dell'agente.</li>
 *     <li><strong>nome</strong>: nome completo dell'agente.</li>
 *     <li><strong>email</strong>: indirizzo email associato.</li>
 *     <li><strong>tipoAgente</strong>: tipologia dell'agente, rappresentata da {@link TipoAgente}.</li>
 * </ul>
 *
 * <p>
 * La classe fornisce costruttori per creare nuove istanze sia con ID
 * predefinito (per oggetti già persistiti), sia senza ID (per nuove entità
 * da salvare). Include inoltre i metodi getter e setter standard.
 * </p>
 */

public class Agente {

    @JsonIgnore
    private Long id;

    private String nome;
    private String email;

    // 👉 Nome corrigido para bater com o DTO e o JSON
    private TipoAgente tipoAgente;

    public Agente(Long id, String nome, String email, TipoAgente tipoAgente) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.tipoAgente = tipoAgente;
    }

    public Agente(String nome, String email, TipoAgente tipoAgente) {
        this(null, nome, email, tipoAgente);
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public TipoAgente getTipoAgente() { return tipoAgente; }

    public void setId(Long id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setEmail(String email) { this.email = email; }
    public void setTipoAgente(TipoAgente tipoAgente) { this.tipoAgente = tipoAgente; }
}
