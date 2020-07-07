package com.polydome.godemon.discordbot;

public interface GodsDataProvider {
    GodData findById(int id);
    GodData findByEmote(String emoteId);
}
