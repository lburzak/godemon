package com.polydome.godemon.discordbot.view.action;

public interface MessageActionRegistry {
    void setAction(long messageId, Action action);
    void setActionArg(long messageId, int index, int arg);
    void clearAction(long messageId);
}
