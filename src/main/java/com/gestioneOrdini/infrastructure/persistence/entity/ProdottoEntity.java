package com.gestioneOrdini.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entità JPA che rappresenta un prodotto nella base dati.
 * <p>
 * Questa classe mappa la tabella <strong>prodotti</strong> del database e viene
 * utilizzata come struttura di persistenza all'interno dell'infrastruttura.
 * Ogni istanza di {@code ProdottoEntity} corrisponde a un record persistito.
 * </p>
 *
 * <p><strong>Campi principali:</strong></p>
 * <ul>
 *     <li><strong>id</strong>: chiave primaria generata automaticamente.</li>
 *     <li><strong>codice</strong>: codice identificativo del prodotto, obbligatorio e univoco.</li>
 *     <li><strong>descrizione</strong>: descrizione del prodotto.</li>
 *     <li><strong>valoreAcquisto</strong>: valore di acquisto del prodotto.</li>
 *     <li><strong>valoreVendita</strong>: valore di vendita del prodotto.</li>
 *     <li><strong>quantita</strong>: quantità attuale disponibile in magazzino.</li>
 *     <li><strong>scortaMinima</strong>: quantità minima di sicurezza; quando la quantità scende
 *         sotto questo valore, il sistema può generare notifiche o inviare email.</li>
 *     <li><strong>archiviato</strong>: indica se il prodotto è archiviato, default false.</li>
 *     <li><strong>dataRegistrazione</strong>: data di registrazione del prodotto nel sistema.</li>
 * </ul>
 *
 * <p>
 * Questa entità viene tipicamente convertita in un oggetto di dominio
 * {@code Prodotto} tramite mapper o adattatori, mantenendo la separazione
 * tra livello di dominio e livello di persistenza.
 * </p>
 */

@Entity
@Table(name = "prodotti")
public class ProdottoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;


    @Column(name = "code", nullable = false, unique = true, updatable = false, length = 6)
    private String codice;

    @Column(name = "description", nullable = false, length = 30)
    private String descrizione;

    @Column(name = "purchase_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal valoreAcquisto;

    @Column(name = "sale_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal valoreVendita;

    @Column(name = "quantity", nullable = false)
    private Integer quantita;

    @Column(name = "minimum_stock", nullable = false)
    private Integer scortaMinima;

    @Column(name = "archived", nullable = false)
    private Boolean archiviato = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime dataRegistrazione;

    public ProdottoEntity() {
    }

    public ProdottoEntity(
            Long id,
            String codice,
            String descrizione,
            BigDecimal valoreAcquisto,
            BigDecimal valoreVendita,
            Integer quantita,
            Integer scortaMinima,
            Boolean archiviato,
            LocalDateTime dataRegistrazione
    ) {
        this.id = id;
        this.codice = codice;
        this.descrizione = descrizione;
        this.valoreAcquisto = valoreAcquisto;
        this.valoreVendita = valoreVendita;
        this.quantita = quantita;
        this.scortaMinima = scortaMinima;
        this.archiviato = archiviato;
        this.dataRegistrazione = dataRegistrazione;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodice() {
        return codice;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public BigDecimal getValoreAcquisto() {
        return valoreAcquisto;
    }

    public BigDecimal getValoreVendita() {
        return valoreVendita;
    }

    public Integer getQuantita() {
        return quantita;
    }

    public Integer getScortaMinima() {
        return scortaMinima;
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
        if (!(o instanceof ProdottoEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ProdottoEntity{" +
                "id=" + id +
                ", codice='" + codice + '\'' +
                ", descrizione='" + descrizione + '\'' +
                '}';
    }
}
