package com.polydome.godemon.discordbot.reaction;

public interface ActionCodeStorage {
    void setCode(long messageId, int code);
    Integer getCode(long messageId);
    void clearCode(long messageId);
}
