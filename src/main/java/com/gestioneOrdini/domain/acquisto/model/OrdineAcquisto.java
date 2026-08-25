package com.gestioneOrdini.domain.acquisto.model;

import com.gestioneOrdini.domain.acquisto.exception.OrdineAcquistoNonModificabileException;
import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Testata di un ordine di acquisto. */
public class OrdineAcquisto {

    private final Long id;
    private final String numeroOrdine;
    private Agente fornitore;
    private final LocalDateTime dataRegistrazione;
    private LocalDate dataRicevimento;
    private LocalDate dataAnnullamento;

    public OrdineAcquisto(Agente fornitore) {
        this(null, null, fornitore, null, null, null);
    }

    public OrdineAcquisto(Long id, String numeroOrdine, Agente fornitore,
                          LocalDateTime dataRegistrazione, LocalDate dataRicevimento,
                          LocalDate dataAnnullamento) {
        this.id = id;
        this.numeroOrdine = numeroOrdine;
        setFornitore(fornitore);
        this.dataRegistrazione = dataRegistrazione;
        if (dataRicevimento != null && dataAnnullamento != null) {
            throw new IllegalArgumentException(
                    "Un ordine di acquisto non può essere ricevuto e annullato");
        }
        this.dataRicevimento = dataRicevimento;
        this.dataAnnullamento = dataAnnullamento;
    }

    public Long getId() { return id; }
    public String getNumeroOrdine() { return numeroOrdine; }
    public Agente getFornitore() { return fornitore; }
    public LocalDateTime getDataRegistrazione() { return dataRegistrazione; }
    public LocalDate getDataRicevimento() { return dataRicevimento; }
    public LocalDate getDataAnnullamento() { return dataAnnullamento; }

    public void setFornitore(Agente fornitore) {
        if (fornitore == null) {
            throw new IllegalArgumentException("Il fornitore è obbligatorio");
        }
        if (fornitore.getTipoAgente() != TipoAgente.FORNITORE) {
            throw new IllegalArgumentException(
                    "L'agente selezionato come fornitore deve essere di tipo FORNITORE");
        }
        this.fornitore = fornitore;
    }

    public void verificaModificabile() {
        if (dataRicevimento != null) {
            throw new OrdineAcquistoNonModificabileException(
                    numeroOrdine, "è già stato ricevuto");
        }
        if (dataAnnullamento != null) {
            throw new OrdineAcquistoNonModificabileException(
                    numeroOrdine, "è già stato annullato");
        }
    }

    public void ricevi(LocalDate data) {
        verificaModificabile();
        if (data == null) {
            throw new IllegalArgumentException("La data di ricevimento è obbligatoria");
        }
        dataRicevimento = data;
    }

    public void annulla(LocalDate data) {
        verificaModificabile();
        if (data == null) {
            throw new IllegalArgumentException("La data di annullamento è obbligatoria");
        }
        dataAnnullamento = data;
    }
}
