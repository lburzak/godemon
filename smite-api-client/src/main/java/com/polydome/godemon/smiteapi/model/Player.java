package com.polydome.godemon.smiteapi.model;

import com.squareup.moshi.Json;

public class Player {
    @Json(name = "Id") public final int id;
    @Json(name = "hz_player_name") public final String hiRezName;

    public Player(int id, String hiRezName) {
        this.id = id;
        this.hiRezName = hiRezName;
    }
}
