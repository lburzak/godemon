package com.polydome.godemon.domain.entity;

import lombok.Data;

@Data
public class Match {
    private final int ownGod;
    private final int opponentGod;
    private final byte kills;
    private final byte deaths;
    private final boolean win;
}
