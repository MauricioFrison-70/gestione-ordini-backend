package com.gestioneOrdini.application.dto;

import com.gestioneOrdini.domain.model.TipoAgente;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO utilizzato per restituire al client le informazioni di un agente
 * già registrato nel sistema.
 *
 * <p>Questa classe rappresenta il payload di output inviato al client
 * (ad esempio tramite una risposta REST) e contiene i dati essenziali
 * dell'agente, come l'identificativo univoco, il nome, l'email, il tipo
 * di agente definito da {@link TipoAgente} e se l'agente è inattivo.</p>
 *
 * <p>Il suo scopo principale è separare il modello di dominio dalla
 * rappresentazione esposta esternamente, garantendo maggiore sicurezza,
 * controllo e stabilità dell'API.</p>
 *
 * @author Mauricio
 * @version 1.0
 */

public class AgenteResponse {

    private Long id;
    private String nome;
    private String email;
    private TipoAgente tipoAgente;
    private Boolean archiviato;
    private LocalDateTime dataRegistrazione;

    public AgenteResponse(Long id, String nome, String email, TipoAgente tipoAgente, Boolean archiviato,
                          LocalDateTime dataRegistrazione) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.tipoAgente = tipoAgente;
        this.archiviato = archiviato;
        this.dataRegistrazione = dataRegistrazione;
    }

    public AgenteResponse(Long id, String nome, String email, TipoAgente tipoAgente) {
        this(id, nome, email, tipoAgente, false, LocalDateTime.now());
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public TipoAgente getTipoAgente() { return tipoAgente; }
    public Boolean getArchiviato() { return archiviato; }
    public LocalDateTime getDataRegistrazione() { return dataRegistrazione; }
}
