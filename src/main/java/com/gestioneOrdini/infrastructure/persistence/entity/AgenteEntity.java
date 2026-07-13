package com.gestioneOrdini.infrastructure.persistence.entity;

import com.gestioneOrdini.domain.model.TipoAgente;
import jakarta.persistence.*;

/**
 * Entità JPA che rappresenta un agente nella base dati.
 * <p>
 * Questa classe mappa la tabella <strong>agenti</strong> del database e viene
 * utilizzata come struttura di persistenza all'interno dell'infrastruttura.
 * Ogni istanza di {@code AgenteEntity} corrisponde a un record persistito.
 * </p>
 *
 * <p><strong>Campi principali:</strong></p>
 * <ul>
 *     <li><strong>id</strong>: chiave primaria generata automaticamente.</li>
 *     <li><strong>nome</strong>: nome dell'agente, obbligatorio e con lunghezza massima di 60 caratteri.</li>
 *     <li><strong>email</strong>: indirizzo email associato all'agente.</li>
 *     <li><strong>tipoAg</strong>: tipologia dell'agente, rappresentata da {@link TipoAgente}.</li>
 * </ul>
 *
 * <p>
 * Questa entità viene tipicamente convertita in un oggetto di dominio
 * {@code Agente} tramite mapper o adattatori, mantenendo la separazione
 * tra livello di dominio e livello di persistenza.
 * </p>
 */

@Entity
@Table(
        name = "agenti",
        indexes = {
                @Index(name = "idx_agenti_nome", columnList = "nome")
        })

public class AgenteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 60, nullable = false)
    private String nome;

    @Column(length = 80, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    private TipoAgente tipoAgente;

    public AgenteEntity() {}

    public AgenteEntity(Long id, String nome, String email, TipoAgente tipoAgente) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.tipoAgente = tipoAgente;
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
