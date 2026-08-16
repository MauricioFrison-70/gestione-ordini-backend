package com.gestioneOrdini.application.agente.dto;

import com.gestioneOrdini.domain.agente.model.TipoAgente;

import java.time.LocalDateTime;

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
public record AgenteResponse(
        Long id,
        String nome,
        String email,
        TipoAgente tipoAgente,
        Boolean archiviato,
        LocalDateTime dataRegistrazione
) {}