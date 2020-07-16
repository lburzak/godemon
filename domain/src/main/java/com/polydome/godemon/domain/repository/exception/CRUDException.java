package com.polydome.godemon.domain.repository.exception;

public class CRUDException extends RuntimeException {
    public CRUDException(String message, Throwable cause) {
        super(message, cause);
    }

    public CRUDException(String message) {
        super(message);
    }
}
