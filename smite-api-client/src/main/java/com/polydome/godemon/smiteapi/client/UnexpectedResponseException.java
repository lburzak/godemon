package com.polydome.godemon.smiteapi.client;

import lombok.Getter;

class UnexpectedResponseException extends Exception {
    @Getter
    private final String rawResponse;

    public UnexpectedResponseException(String message, String rawResponse) {
        super(message);
        this.rawResponse = rawResponse;
    }
}
