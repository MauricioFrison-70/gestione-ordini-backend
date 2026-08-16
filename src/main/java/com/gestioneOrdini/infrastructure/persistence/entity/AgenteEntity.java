package com.gestioneOrdini.infrastructure.persistence.entity;

import com.gestioneOrdini.domain.agente.model.TipoAgente;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

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
 *     <li><strong>archiviato</strong>: indica se l'agente è archiviato, default false.</li>
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
                @Index(name = "idx_agenti_name", columnList = "name")
        })

public class AgenteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 60, nullable = false)
    private String nome;

    @Column(name = "email", length = 80, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "agent_type", nullable = false)
    private TipoAgente tipoAgente;

    @Column(name = "is_archived", nullable = false, columnDefinition = "BIT DEFAULT 0")
    private Boolean archiviato = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime dataRegistrazione;

    public AgenteEntity() {
    }

    public AgenteEntity(
            Long id,
            String nome,
            String email,
            TipoAgente tipoAgente,
            Boolean archiviato
    ) {
        this(id, nome, email, tipoAgente, archiviato, null);
    }

    public AgenteEntity(
            Long id,
            String nome,
            String email,
            TipoAgente tipoAgente,
            Boolean archiviato,
            LocalDateTime dataRegistrazione
    ) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.tipoAgente = tipoAgente;
        this.archiviato = archiviato;
        this.dataRegistrazione = dataRegistrazione;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
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

    public Boolean getArchiviato() {
        return archiviato;
    }

    public void setArchiviato(Boolean archiviato) {
        this.archiviato = archiviato;
    }

    public LocalDateTime getDataRegistrazione() {
        return dataRegistrazione;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AgenteEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AgenteEntity{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}

