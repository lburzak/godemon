package com.polydome.godemon.smitedata.implementation;

import com.polydome.godemon.domain.entity.GameMode;
import com.polydome.godemon.smiteapi.model.Queue;

public class SmiteGameModeService {
    public int getGameModeId(GameMode gameMode) {
        return switch (gameMode) {
            case RANKED_DUEL -> Queue.RANKED_DUEL.getId();
        };
    }

    public GameMode getGameModeFromId(int id) {
        return switch (Queue.fromId(id)) {
            case RANKED_DUEL -> GameMode.RANKED_DUEL;
            default -> throw new UnsupportedOperationException(String.format("Unable to match GameMode to Queue[%d]", id));
        };
    }
}
