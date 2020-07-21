package com.polydome.godemon.discordbot.reaction;

public interface MessageActionRegistry {
    void setAction(long messageId, Action action);
    void setActionArg(long messageId, int index, int arg);
    void clearAction(long messageId);
}
