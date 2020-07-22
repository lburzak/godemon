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
        Queue queue;

        switch (id) {
            case 440: queue = RANKED_DUEL; break;
            case 448: queue = JOUST; break;
            default: queue = UNKNOWN; break;
        }

        return queue;
    }
}
