package com.gestioneOrdini.application.assistente.port;

/**
 * Fornisce all'assistente un riepilogo controllato e aggiornato degli ordini di vendita.
 * L'implementazione non accetta SQL o nomi di oggetti provenienti dal modello IA.
 */
public interface ContestoDatiOrdiniVendita {
    String contenutoCorrente();
}
