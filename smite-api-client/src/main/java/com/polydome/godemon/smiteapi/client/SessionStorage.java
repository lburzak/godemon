package com.polydome.godemon.smiteapi.client;

public interface SessionStorage {
    void setSessionId(String id, int expirationMinutes);
    String getSessionId();
    boolean existsSession();
}
