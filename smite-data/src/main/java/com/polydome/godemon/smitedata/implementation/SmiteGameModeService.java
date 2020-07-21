package com.polydome.godemon.smitedata.implementation;

import com.polydome.godemon.domain.entity.GameMode;
import com.polydome.godemon.smiteapi.model.Queue;

public class SmiteGameModeService {
    public int getGameModeId(GameMode gameMode) {
        return switch (gameMode) {
            case RANKED_DUEL -> Queue.RANKED_DUEL.getId();
            case JOUST -> Queue.JOUST.getId();
        };
    }

    public GameMode getGameModeFromId(int id) {
        return switch (Queue.fromId(id)) {
            case RANKED_DUEL -> GameMode.RANKED_DUEL;
            case JOUST -> GameMode.JOUST;
            case UNKNOWN -> throw new UnsupportedOperationException("Unknown queue id " + id);
        };
    }
}
