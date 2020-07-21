package com.polydome.godemon.discordbot.view;

import com.polydome.godemon.domain.entity.GameMode;

public enum Queue {
    RANKED_DUEL("Ranked Duel"),
    JOUST("Joust");

    private final static Queue[] queues = Queue.values();
    public final static int lastIndex = queues.length - 1;

    private final String visibleName;

    Queue(String visibleName) {
        this.visibleName = visibleName;
    }

    public static Queue fromIndex(int index) {
        return queues[index];
    }

    public String getVisibleName() {
        return visibleName;
    }

    public GameMode toGameMode() {
        switch (this) {
            case RANKED_DUEL: return GameMode.RANKED_DUEL;
            case JOUST: return GameMode.JOUST;
        }

        return null;
    }
}
