package com.gestioneOrdini.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;

@Entity
@Table(
        name = "righe_ordini_vendita",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_righe_ordine_prodotto",
                columnNames = {"order_id", "product_id"}),
        indexes = @Index(name = "ix_righe_ordine", columnList = "order_id")
)
@Check(constraints = "quantity > 0 AND unit_price >= 0")
public class RigaOrdineVenditaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_righe_ordine_vendita"))
    private OrdineVenditaEntity ordineVendita;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_righe_prodotto"))
    private ProdottoEntity prodotto;

    @Column(name = "quantity", nullable = false)
    private Integer quantita;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal valoreUnitario;

    protected RigaOrdineVenditaEntity() {}

    public RigaOrdineVenditaEntity(Long id, OrdineVenditaEntity ordineVendita,
                                   ProdottoEntity prodotto, Integer quantita,
                                   BigDecimal valoreUnitario) {
        this.id = id;
        this.ordineVendita = ordineVendita;
        this.prodotto = prodotto;
        this.quantita = quantita;
        this.valoreUnitario = valoreUnitario;
    }

    public Long getId() { return id; }
    public OrdineVenditaEntity getOrdineVendita() { return ordineVendita; }
    public ProdottoEntity getProdotto() { return prodotto; }
    public Integer getQuantita() { return quantita; }
    public BigDecimal getValoreUnitario() { return valoreUnitario; }
}
