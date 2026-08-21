package com.gestioneOrdini.domain.agente.exception;

public class AgenteUtilizzatoException extends RuntimeException {
    public static final String CODICE = "AGENTE_UTILIZZATO";

    public AgenteUtilizzatoException() {
        super("L'agente è utilizzato in uno o più ordini di vendita. "
                + "Vuoi archiviarlo o annullare l'eliminazione?");
    }
}
