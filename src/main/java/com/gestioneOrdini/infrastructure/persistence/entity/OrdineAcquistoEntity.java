package com.gestioneOrdini.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordini_acquisto", uniqueConstraints =
        @UniqueConstraint(name = "uk_ordini_acquisto_numero", columnNames = "order_number"))
public class OrdineAcquistoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", length = 30, unique = true)
    private String numeroOrdine;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ordini_acquisto_fornitore"))
    private AgenteEntity fornitore;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime dataRegistrazione;

    @Column(name = "receipt_date")
    private LocalDate dataRicevimento;

    @Column(name = "cancellation_date")
    private LocalDate dataAnnullamento;

    @OneToMany(mappedBy = "ordineAcquisto", cascade = CascadeType.REMOVE)
    private List<RigaOrdineAcquistoEntity> righe = new ArrayList<>();

    protected OrdineAcquistoEntity() {}

    public OrdineAcquistoEntity(Long id, String numeroOrdine, AgenteEntity fornitore,
                                LocalDateTime dataRegistrazione,
                                LocalDate dataRicevimento, LocalDate dataAnnullamento) {
        this.id = id;
        this.numeroOrdine = numeroOrdine;
        this.fornitore = fornitore;
        this.dataRegistrazione = dataRegistrazione;
        this.dataRicevimento = dataRicevimento;
        this.dataAnnullamento = dataAnnullamento;
    }

    public Long getId() { return id; }
    public String getNumeroOrdine() { return numeroOrdine; }
    public void setNumeroOrdine(String numeroOrdine) { this.numeroOrdine = numeroOrdine; }
    public AgenteEntity getFornitore() { return fornitore; }
    public LocalDateTime getDataRegistrazione() { return dataRegistrazione; }
    public LocalDate getDataRicevimento() { return dataRicevimento; }
    public LocalDate getDataAnnullamento() { return dataAnnullamento; }
}
