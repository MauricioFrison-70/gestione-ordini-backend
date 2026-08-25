package com.gestioneOrdini.domain.ordine.model;

import com.gestioneOrdini.domain.prodotto.model.Prodotto;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Riga appartenente alla testata di un ordine di vendita. */
public class RigaOrdineVendita {

    public static final int QUANTITA_MASSIMA = 999_999_999;
    public static final int DECIMALI_VALORE = 2;

    private final Long id;
    private final Long ordineVenditaId;
    private Prodotto prodotto;
    private Integer quantita;
    private BigDecimal valoreUnitario;

    public RigaOrdineVendita(Long ordineVenditaId, Prodotto prodotto,
                             Integer quantita, BigDecimal valoreUnitario) {
        this(null, ordineVenditaId, prodotto, quantita, valoreUnitario);
    }

    public RigaOrdineVendita(Long id, Long ordineVenditaId, Prodotto prodotto,
                             Integer quantita, BigDecimal valoreUnitario) {
        if (ordineVenditaId == null) {
            throw new IllegalArgumentException("L'ordine di vendita è obbligatorio");
        }
        this.id = id;
        this.ordineVenditaId = ordineVenditaId;
        aggiorna(prodotto, quantita, valoreUnitario);
    }

    public Long getId() { return id; }
    public Long getOrdineVenditaId() { return ordineVenditaId; }
    public Prodotto getProdotto() { return prodotto; }
    public Integer getQuantita() { return quantita; }
    public BigDecimal getValoreUnitario() { return valoreUnitario; }

    public BigDecimal getTotaleRiga() {
        return BigDecimal.valueOf(quantita).multiply(valoreUnitario)
                .setScale(DECIMALI_VALORE, RoundingMode.HALF_UP);
    }

    public void aggiorna(Prodotto prodotto, Integer quantita, BigDecimal valoreUnitario) {
        if (prodotto == null || prodotto.getId() == null) {
            throw new IllegalArgumentException("Il prodotto è obbligatorio");
        }
        validaQuantita(quantita);
        validaValoreUnitario(valoreUnitario);
        this.prodotto = prodotto;
        this.quantita = quantita;
        this.valoreUnitario = valoreUnitario;
    }

    private static void validaQuantita(Integer quantita) {
        if (quantita == null || quantita <= 0) {
            throw new IllegalArgumentException("La quantità deve essere maggiore di zero");
        }
        if (quantita > QUANTITA_MASSIMA) {
            throw new IllegalArgumentException(
                    "La quantità non può superare 999999999");
        }
    }

    private static void validaValoreUnitario(BigDecimal valoreUnitario) {
        if (valoreUnitario == null || valoreUnitario.signum() < 0) {
            throw new IllegalArgumentException(
                    "Il valore unitario deve essere maggiore o uguale a zero");
        }
        if (valoreUnitario.scale() > DECIMALI_VALORE) {
            throw new IllegalArgumentException(
                    "Il valore unitario non può avere più di 2 decimali");
        }
    }
}
