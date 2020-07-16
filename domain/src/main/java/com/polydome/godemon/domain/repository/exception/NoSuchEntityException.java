package com.polydome.godemon.domain.repository.exception;

public class NoSuchEntityException extends CRUDException {
    public NoSuchEntityException(Class<Object> entityClass, String key) {
        super(String.format("Entity %s with key %s doesn't exist", entityClass.getName(), key));
    }
}
