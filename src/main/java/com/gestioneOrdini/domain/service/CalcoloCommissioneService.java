package com.gestioneOrdini.domain.service;

import com.gestioneOrdini.domain.model.Agente;
import java.math.BigDecimal;

/**
 * Servizio di dominio responsabile del calcolo delle commissioni
 * in base al tipo di agente.
 */
public class CalcoloCommissioneService {

    /**
     * Calcola la commissione spettante all'agente.
     *
     * @param agente agente per cui calcolare la commissione
     * @param valoreVendita valore totale della vendita
     * @return commissione calcolata
     */
    public BigDecimal calcola(Agente agente, BigDecimal valoreVendita) {
        return switch (agente.getTipoAgente()) {
            case CLIENTE -> BigDecimal.valueOf(0);
            case TRASPORTATORE -> BigDecimal.valueOf(0);
            case FORNITORE -> BigDecimal.valueOf(0);
            case VENDITORE -> valoreVendita.multiply(BigDecimal.valueOf(0.10));
        };
    }
}

