package com.polydome.godemon.domain.repository.exception;

public class DuplicateEntityException extends CRUDException {
    public DuplicateEntityException(Class<Object> entityClass, String key) {
        super(String.format("Entity %s with key %s already exists", entityClass.getName(), key));
    }

    public DuplicateEntityException(String entityName, String key) {
        super(String.format("Entity %s with key %s already exists", entityName, key));
    }

    public DuplicateEntityException(String entityName) {
        super(String.format("Such %s entity already exists", entityName));
    }
}
