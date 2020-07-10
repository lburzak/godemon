package com.polydome.godemon.domain.entity;

import lombok.Data;

@Data
public class Match {
    private final int ownGod;
    private final int opponentGod;
    private final int kills;
    private final int deaths;
    private final boolean win;
}
