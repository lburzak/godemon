package com.polydome.godemon.domain.repository.exception;

public class DuplicateEntityException extends CRUDException {
    public DuplicateEntityException(Class<Object> entityClass, String key) {
        super(String.format("Entity %s with key %s already exists", entityClass.getName(), key));
    }
}
