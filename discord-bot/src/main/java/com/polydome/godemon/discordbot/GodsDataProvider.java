package com.polydome.godemon.discordbot;

interface GodsDataProvider {
    GodData findById(int id);
    GodData findByEmote(String emoteId);
}
