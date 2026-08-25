package com.gestioneOrdini.domain.ordine.model;

import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import com.gestioneOrdini.domain.ordine.exception.OrdineVenditaNonModificabileException;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Testata (record master) di un ordine di vendita. */
public class OrdineVendita {

    private final Long id;
    private final String numeroOrdine;
    private Agente cliente;
    private Agente venditore;
    private Agente trasportatore;
    private final LocalDateTime dataRegistrazione;
    private LocalDate dataRilascio;
    private LocalDate dataAnnullamento;

    public OrdineVendita(Agente cliente, Agente venditore, Agente trasportatore) {
        this(null, null, cliente, venditore, trasportatore, null, null, null);
    }

    public OrdineVendita(Agente cliente, Agente venditore, Agente trasportatore,
                         LocalDate dataRilascio) {
        this(null, null, cliente, venditore, trasportatore, null, dataRilascio, null);
    }

    public OrdineVendita(Long id, String numeroOrdine, Agente cliente, Agente venditore,
                         Agente trasportatore, LocalDateTime dataRegistrazione,
                         LocalDate dataRilascio) {
        this(id, numeroOrdine, cliente, venditore, trasportatore,
                dataRegistrazione, dataRilascio, null);
    }

    public OrdineVendita(Long id, String numeroOrdine, Agente cliente, Agente venditore,
                         Agente trasportatore, LocalDateTime dataRegistrazione,
                         LocalDate dataRilascio, LocalDate dataAnnullamento) {
        this.id = id;
        this.numeroOrdine = numeroOrdine;
        setCliente(cliente);
        setVenditore(venditore);
        setTrasportatore(trasportatore);
        this.dataRegistrazione = dataRegistrazione;
        if (dataRilascio != null && dataAnnullamento != null) {
            throw new IllegalArgumentException(
                    "Un ordine di vendita non può essere rilasciato e annullato");
        }
        this.dataRilascio = dataRilascio;
        this.dataAnnullamento = dataAnnullamento;
    }

    public Long getId() { return id; }
    public String getNumeroOrdine() { return numeroOrdine; }
    public Agente getCliente() { return cliente; }
    public Agente getVenditore() { return venditore; }
    public Agente getTrasportatore() { return trasportatore; }
    public LocalDateTime getDataRegistrazione() { return dataRegistrazione; }
    public LocalDate getDataRilascio() { return dataRilascio; }
    public LocalDate getDataAnnullamento() { return dataAnnullamento; }

    public void setCliente(Agente cliente) {
        validaTipo(cliente, TipoAgente.CLIENTE, "cliente");
        this.cliente = cliente;
    }

    public void setVenditore(Agente venditore) {
        validaTipo(venditore, TipoAgente.VENDITORE, "venditore");
        this.venditore = venditore;
    }

    public void setTrasportatore(Agente trasportatore) {
        validaTipo(trasportatore, TipoAgente.TRASPORTATORE, "trasportatore");
        this.trasportatore = trasportatore;
    }

    public void verificaModificabile() {
        if (dataRilascio != null) {
            throw new OrdineVenditaNonModificabileException(
                    numeroOrdine, "è già stato rilasciato");
        }
        if (dataAnnullamento != null) {
            throw new OrdineVenditaNonModificabileException(
                    numeroOrdine, "è già stato annullato");
        }
    }

    public void rilascia(LocalDate data) {
        verificaModificabile();
        if (data == null) {
            throw new IllegalArgumentException("La data di rilascio è obbligatoria");
        }
        dataRilascio = data;
    }

    public void annulla(LocalDate data) {
        verificaModificabile();
        if (data == null) {
            throw new IllegalArgumentException("La data di annullamento è obbligatoria");
        }
        dataAnnullamento = data;
    }

    private static void validaTipo(Agente agente, TipoAgente atteso, String campo) {
        if (agente == null) {
            throw new IllegalArgumentException("Il " + campo + " è obbligatorio");
        }
        if (agente.getTipoAgente() != atteso) {
            throw new IllegalArgumentException(
                    "L'agente selezionato come " + campo + " deve essere di tipo " + atteso);
        }
    }
}
