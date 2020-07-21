package com.polydome.godemon.discordbot.reaction;

public interface ActionArgStorage {
    void setIntArg(long messageId, int index, int arg);
    Integer getIntArg(long messageId, int index) throws IllegalStateException;
}
