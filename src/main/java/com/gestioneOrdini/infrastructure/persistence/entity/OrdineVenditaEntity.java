package com.gestioneOrdini.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ordini_vendita", uniqueConstraints =
        @UniqueConstraint(name = "uk_ordini_vendita_numero", columnNames = "order_number"))
public class OrdineVenditaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Assegnato immediatamente dopo l'INSERT, quando l'IDENTITY è disponibile.
    @Column(name = "order_number", length = 30, unique = true)
    private String numeroOrdine;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ordini_vendita_cliente"))
    private AgenteEntity cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ordini_vendita_venditore"))
    private AgenteEntity venditore;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrier_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ordini_vendita_trasportatore"))
    private AgenteEntity trasportatore;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime dataRegistrazione;

    @Column(name = "release_date")
    private LocalDate dataRilascio;

    public OrdineVenditaEntity() {}

    public OrdineVenditaEntity(Long id, String numeroOrdine, AgenteEntity cliente,
                               AgenteEntity venditore, AgenteEntity trasportatore,
                               LocalDateTime dataRegistrazione, LocalDate dataRilascio) {
        this.id = id;
        this.numeroOrdine = numeroOrdine;
        this.cliente = cliente;
        this.venditore = venditore;
        this.trasportatore = trasportatore;
        this.dataRegistrazione = dataRegistrazione;
        this.dataRilascio = dataRilascio;
    }

    public Long getId() { return id; }
    public String getNumeroOrdine() { return numeroOrdine; }
    public void setNumeroOrdine(String numeroOrdine) { this.numeroOrdine = numeroOrdine; }
    public AgenteEntity getCliente() { return cliente; }
    public AgenteEntity getVenditore() { return venditore; }
    public AgenteEntity getTrasportatore() { return trasportatore; }
    public LocalDateTime getDataRegistrazione() { return dataRegistrazione; }
    public LocalDate getDataRilascio() { return dataRilascio; }
}
