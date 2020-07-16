package com.polydome.godemon.domain.repository.exception;

public class DuplicateEntryException extends CRUDException {
    public DuplicateEntryException(Class<Object> entityClass, String key) {
        super(String.format("Entity %s with key %s already exists", entityClass.getName(), key));
    }

    public DuplicateEntryException(String entityName, String key) {
        super(String.format("Entity %s with key %s already exists", entityName, key));
    }

    public DuplicateEntryException(String entityName) {
        super(String.format("Such %s entity already exists", entityName));
    }
}
