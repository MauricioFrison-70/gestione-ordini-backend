package com.gestioneOrdini.domain.prodotto.event;

import java.math.BigDecimal;

/**
 * Evento di dominio che rappresenta la creazione di un nuovo prodotto.
 */
public class ProdottoCreatoEvent {

    private final Long idProdotto;
    private final String codice;
    private final String descrizione;
    private final BigDecimal valoreAcquisto;
    private final BigDecimal valoreVendita;
    private final Integer quantita;
    private final Integer scortaMinima;

    public ProdottoCreatoEvent(
            Long idProdotto,
            String codice,
            String descrizione,
            BigDecimal valoreAcquisto,
            BigDecimal valoreVendita,
            Integer quantita,
            Integer scortaMinima) {

        this.idProdotto = idProdotto;
        this.codice = codice;
        this.descrizione = descrizione;
        this.valoreAcquisto = valoreAcquisto;
        this.valoreVendita = valoreVendita;
        this.quantita = quantita;
        this.scortaMinima = scortaMinima;
    }

    public Long getIdProdotto() {
        return idProdotto;
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
}
