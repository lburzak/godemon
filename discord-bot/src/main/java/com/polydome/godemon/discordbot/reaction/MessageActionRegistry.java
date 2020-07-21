package com.polydome.godemon.discordbot.reaction;

public interface MessageActionRegistry {
    void setAction(long messageId, Action action);
    void clearAction(long messageId);
}
