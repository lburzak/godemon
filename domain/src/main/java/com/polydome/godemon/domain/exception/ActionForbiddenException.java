package com.polydome.godemon.domain.exception;

public class ActionForbiddenException extends RuntimeException {
    public ActionForbiddenException(String message) {
        super(message);
    }
}
