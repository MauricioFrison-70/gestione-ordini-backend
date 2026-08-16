package com.gestioneOrdini.domain.prodotto.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Modello di dominio che rappresenta un prodotto nel sistema.
 * <p>
 * Contiene le informazioni principali utilizzate dai casi d'uso,
 * mantenendo il dominio indipendente dalla persistenza.
 * </p>
 */
public class Prodotto {

    private final Long id;
    private final String codice;
    private String descrizione;
    private BigDecimal valoreAcquisto;
    private BigDecimal valoreVendita;
    private Integer quantita;
    private Integer scortaMinima;
    private Boolean archiviato = false;
    private final LocalDateTime dataRegistrazione;

    /**
     * Costruttore completo, utilizzato per ricostruire il modello
     * a partire da dati già persistiti.
     */
    public Prodotto(Long id,
                    String codice,
                    String descrizione,
                    BigDecimal valoreAcquisto,
                    BigDecimal valoreVendita,
                    Integer quantita,
                    Integer scortaMinima,
                    Boolean archiviato) {
        this(id, codice, descrizione, valoreAcquisto, valoreVendita, quantita,
                scortaMinima, archiviato, null);
    }

    /**
     * Ricostruisce un prodotto già persistito, inclusa la data di registrazione
     * assegnata dall'infrastruttura di persistenza.
     */
    public Prodotto(Long id,
                    String codice,
                    String descrizione,
                    BigDecimal valoreAcquisto,
                    BigDecimal valoreVendita,
                    Integer quantita,
                    Integer scortaMinima,
                    Boolean archiviato,
                    LocalDateTime dataRegistrazione) {

        this.id = id;
        this.codice = validaTestoObbligatorio(codice, "Il codice è obbligatorio");
        setDescrizione(descrizione);
        setValoreAcquisto(valoreAcquisto);
        setValoreVendita(valoreVendita);
        setQuantita(quantita);
        setScortaMinima(scortaMinima);
        this.archiviato = archiviato;
        this.dataRegistrazione = dataRegistrazione;
    }

    /**
     * Costruttore per nuovi prodotti non ancora persistiti.
     */
    public Prodotto(String codice,
                    String descrizione,
                    BigDecimal valoreAcquisto,
                    BigDecimal valoreVendita,
                    Integer quantita,
                    Integer scortaMinima,
                    Boolean archiviato) {

        this(null, codice, descrizione, valoreAcquisto, valoreVendita,
                quantita, scortaMinima, archiviato);
    }

    public Long getId() {
        return id;
    }

    public String getCodice() {
        return codice;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = validaTestoObbligatorio(descrizione, "La descrizione è obbligatoria");
    }

    public BigDecimal getValoreAcquisto() {
        return valoreAcquisto;
    }

    public void setValoreAcquisto(BigDecimal valoreAcquisto) {
        validaValoreNonNegativo(valoreAcquisto, "Il valore di acquisto deve essere maggiore o uguale a zero");
        this.valoreAcquisto = valoreAcquisto;
    }

    public BigDecimal getValoreVendita() {
        return valoreVendita;
    }

    public void setValoreVendita(BigDecimal valoreVendita) {
        validaValoreNonNegativo(valoreVendita, "Il valore di vendita deve essere maggiore o uguale a zero");
        this.valoreVendita = valoreVendita;
    }

    public Integer getQuantita() {
        return quantita;
    }

    public void setQuantita(Integer quantita) {
        validaInteroNonNegativo(quantita, "La quantità deve essere maggiore o uguale a zero");
        this.quantita = quantita;
    }

    public Integer getScortaMinima() {
        return scortaMinima;
    }

    public void setScortaMinima(Integer scortaMinima) {
        validaInteroNonNegativo(scortaMinima, "La scorta minima deve essere maggiore o uguale a zero");
        this.scortaMinima = scortaMinima;
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

    private static String validaTestoObbligatorio(String valore, String messaggio) {
        if (valore == null || valore.isBlank()) {
            throw new IllegalArgumentException(messaggio);
        }
        return valore;
    }

    private static void validaValoreNonNegativo(BigDecimal valore, String messaggio) {
        if (valore == null || valore.signum() < 0) {
            throw new IllegalArgumentException(messaggio);
        }
    }

    private static void validaInteroNonNegativo(Integer valore, String messaggio) {
        if (valore == null || valore < 0) {
            throw new IllegalArgumentException(messaggio);
        }
    }
}
