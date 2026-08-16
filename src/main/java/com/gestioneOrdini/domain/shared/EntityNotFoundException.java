package com.gestioneOrdini.domain.shared;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String entityName, Object id) {
        super(entityName + " con id " + id + " non trovato");
    }
}