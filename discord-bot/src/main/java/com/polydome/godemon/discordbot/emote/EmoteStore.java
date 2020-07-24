package com.polydome.godemon.discordbot.emote;

public interface EmoteStore {
    long findId(String name);
    boolean has(String name);
}
