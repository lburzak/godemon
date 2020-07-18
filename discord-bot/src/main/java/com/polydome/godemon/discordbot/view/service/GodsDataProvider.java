package com.polydome.godemon.discordbot.view.service;

public interface GodsDataProvider {
    // TODO: Simplify
    GodData findById(int id);
    GodData findByEmote(String emoteId);
}
