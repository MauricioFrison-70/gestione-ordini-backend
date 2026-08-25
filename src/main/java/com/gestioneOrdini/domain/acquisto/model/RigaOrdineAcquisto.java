package com.gestioneOrdini.domain.acquisto.model;

import com.gestioneOrdini.domain.prodotto.model.Prodotto;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Riga appartenente a un ordine di acquisto. */
public class RigaOrdineAcquisto {

    public static final int QUANTITA_MASSIMA = 999_999_999;

    private final Long id;
    private final Long ordineAcquistoId;
    private Prodotto prodotto;
    private Integer quantita;
    private BigDecimal valoreUnitario;

    public RigaOrdineAcquisto(Long ordineAcquistoId, Prodotto prodotto,
                              Integer quantita, BigDecimal valoreUnitario) {
        this(null, ordineAcquistoId, prodotto, quantita, valoreUnitario);
    }

    public RigaOrdineAcquisto(Long id, Long ordineAcquistoId, Prodotto prodotto,
                              Integer quantita, BigDecimal valoreUnitario) {
        if (ordineAcquistoId == null) {
            throw new IllegalArgumentException("L'ordine di acquisto è obbligatorio");
        }
        this.id = id;
        this.ordineAcquistoId = ordineAcquistoId;
        aggiorna(prodotto, quantita, valoreUnitario);
    }

    public Long getId() { return id; }
    public Long getOrdineAcquistoId() { return ordineAcquistoId; }
    public Prodotto getProdotto() { return prodotto; }
    public Integer getQuantita() { return quantita; }
    public BigDecimal getValoreUnitario() { return valoreUnitario; }

    public BigDecimal getTotaleRiga() {
        return BigDecimal.valueOf(quantita).multiply(valoreUnitario)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void aggiorna(
            Prodotto prodotto,
            Integer quantita,
            BigDecimal valoreUnitario) {
        if (prodotto == null || prodotto.getId() == null) {
            throw new IllegalArgumentException("Il prodotto è obbligatorio");
        }
        if (quantita == null || quantita <= 0 || quantita > QUANTITA_MASSIMA) {
            throw new IllegalArgumentException(
                    "La quantità deve essere compresa tra 1 e 999999999");
        }
        if (valoreUnitario == null || valoreUnitario.signum() < 0
                || valoreUnitario.scale() > 2) {
            throw new IllegalArgumentException(
                    "Il valore unitario deve essere maggiore o uguale a zero "
                            + "e avere al massimo 2 decimali");
        }
        this.prodotto = prodotto;
        this.quantita = quantita;
        this.valoreUnitario = valoreUnitario;
    }
}
