package com.gestioneOrdini.domain.ordine.model;

import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;

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

    public OrdineVendita(Agente cliente, Agente venditore, Agente trasportatore,
                         LocalDate dataRilascio) {
        this(null, null, cliente, venditore, trasportatore, null, dataRilascio);
    }

    public OrdineVendita(Long id, String numeroOrdine, Agente cliente, Agente venditore,
                         Agente trasportatore, LocalDateTime dataRegistrazione,
                         LocalDate dataRilascio) {
        this.id = id;
        this.numeroOrdine = numeroOrdine;
        setCliente(cliente);
        setVenditore(venditore);
        setTrasportatore(trasportatore);
        this.dataRegistrazione = dataRegistrazione;
        this.dataRilascio = dataRilascio;
    }

    public Long getId() { return id; }
    public String getNumeroOrdine() { return numeroOrdine; }
    public Agente getCliente() { return cliente; }
    public Agente getVenditore() { return venditore; }
    public Agente getTrasportatore() { return trasportatore; }
    public LocalDateTime getDataRegistrazione() { return dataRegistrazione; }
    public LocalDate getDataRilascio() { return dataRilascio; }

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

    public void setDataRilascio(LocalDate dataRilascio) {
        this.dataRilascio = dataRilascio;
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
