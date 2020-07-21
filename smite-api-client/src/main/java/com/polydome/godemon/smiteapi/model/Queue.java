package com.polydome.godemon.smiteapi.model;

import lombok.Getter;

public enum Queue {
    RANKED_DUEL(440),
    JOUST(448),
    UNKNOWN(0);

    @Getter
    private final int id;

    Queue(int id) {
        this.id = id;
    }

    public static Queue fromId(int id) {
        return switch (id) {
            case 440 -> RANKED_DUEL;
            case 448 -> JOUST;
            default -> UNKNOWN;
        };
    }
}
