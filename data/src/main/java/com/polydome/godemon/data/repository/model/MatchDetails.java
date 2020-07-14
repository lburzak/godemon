package com.polydome.godemon.data.repository.model;

import lombok.Data;

@Data
public class MatchDetails {
    public enum Mode {
        RANKED_DUEL
    }

    private final byte participantsCount;
    private final PlayerRecord[] players;
}
