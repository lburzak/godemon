package com.polydome.godemon.smitedata.entity;

public class SmiteChampion extends God {
    public final String emoteId;

    public SmiteChampion(Integer id, String name, String displayName, String emoteId) {
        super(id, name, displayName);
        this.emoteId = emoteId;
    }
}
