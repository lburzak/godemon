package com.polydome.godemon.domain.service.matchdetails;

import lombok.Data;

@Data
public class PlayerRecord {
    public enum WinStatus {
        WINNER,
        LOSER
    }

    private final int playerId;
    private final String playerName;
    private final byte kills;
    private final byte deaths;
    private final int godId;
    private final WinStatus winStatus;
}
