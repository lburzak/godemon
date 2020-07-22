package com.polydome.godemon.smitedata.implementation;

import com.polydome.godemon.domain.entity.GameMode;
import com.polydome.godemon.smiteapi.model.Queue;

public class SmiteGameModeService {
    public int getGameModeId(GameMode gameMode) {
        int id;

        switch (gameMode) {
            case RANKED_DUEL: id = Queue.RANKED_DUEL.getId(); break;
            case JOUST: id = Queue.JOUST.getId(); break;
            default: id = -1;
        }

        return id;
    }

    public GameMode getGameModeFromId(int id) {
        GameMode gameMode;

        switch (Queue.fromId(id)) {
            case RANKED_DUEL: gameMode = GameMode.RANKED_DUEL; break;
            case JOUST: gameMode = GameMode.JOUST; break;
            default: throw new UnsupportedOperationException("Unknown queue id " + id);
        }

        return gameMode;
    }
}
