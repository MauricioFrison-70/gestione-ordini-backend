package com.gestioneOrdini.domain.prodotto.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Modello di dominio che rappresenta un prodotto nel sistema.
 */
public class Prodotto {

    public static final int LUNGHEZZA_MASSIMA_CODICE = 6;
    public static final int LUNGHEZZA_MASSIMA_DESCRIZIONE = 30;

    private final Long id;
    private final String codice;
    private String descrizione;
    private BigDecimal valoreAcquisto;
    private BigDecimal valoreVendita;
    private Integer quantita;
    private Integer scortaMinima;
    private Boolean archiviato = false;
    private final LocalDateTime dataRegistrazione;

    public Prodotto(
            Long id,
            String codice,
            String descrizione,
            BigDecimal valoreAcquisto,
            BigDecimal valoreVendita,
            Integer quantita,
            Integer scortaMinima,
            Boolean archiviato
    ) {
        this(id, codice, descrizione, valoreAcquisto, valoreVendita, quantita,
                scortaMinima, archiviato, null);
    }

    public Prodotto(
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
        this.codice = validaTestoObbligatorioELunghezzaMassima(
                codice,
                "Il codice è obbligatorio",
                "Il codice non può superare " + LUNGHEZZA_MASSIMA_CODICE + " caratteri",
                LUNGHEZZA_MASSIMA_CODICE
        );
        setDescrizione(descrizione);
        setValoreAcquisto(valoreAcquisto);
        setValoreVendita(valoreVendita);
        setQuantita(quantita);
        setScortaMinima(scortaMinima);
        this.archiviato = archiviato;
        this.dataRegistrazione = dataRegistrazione;
    }

    public Prodotto(
            String codice,
            String descrizione,
            BigDecimal valoreAcquisto,
            BigDecimal valoreVendita,
            Integer quantita,
            Integer scortaMinima,
            Boolean archiviato
    ) {
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
        this.descrizione = validaTestoObbligatorioELunghezzaMassima(
                descrizione,
                "La descrizione è obbligatoria",
                "La descrizione non può superare " + LUNGHEZZA_MASSIMA_DESCRIZIONE + " caratteri",
                LUNGHEZZA_MASSIMA_DESCRIZIONE
        );
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

    public void incrementaQuantita(Integer quantitaRicevuta) {
        if (quantitaRicevuta == null || quantitaRicevuta <= 0) {
            throw new IllegalArgumentException(
                    "La quantità ricevuta deve essere maggiore di zero");
        }
        try {
            setQuantita(Math.addExact(this.quantita, quantitaRicevuta));
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException(
                    "La quantità in magazzino supera il limite consentito", ex);
        }
    }

    public void decrementaQuantita(Integer quantitaVenduta) {
        if (quantitaVenduta == null || quantitaVenduta <= 0) {
            throw new IllegalArgumentException(
                    "La quantità venduta deve essere maggiore di zero");
        }
        if (this.quantita < quantitaVenduta) {
            throw new IllegalArgumentException(
                    "La quantità disponibile non è sufficiente");
        }
        setQuantita(this.quantita - quantitaVenduta);
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

    private static String validaTestoObbligatorioELunghezzaMassima(
            String valore,
            String messaggioObbligatorio,
            String messaggioLunghezza,
            int lunghezzaMassima
    ) {
        if (valore == null || valore.isBlank()) {
            throw new IllegalArgumentException(messaggioObbligatorio);
        }
        if (valore.length() > lunghezzaMassima) {
            throw new IllegalArgumentException(messaggioLunghezza);
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
