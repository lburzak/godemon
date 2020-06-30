package com.polydome.godemon.domain.entity;

import lombok.Data;

@Data
public class Challenger {
    private final String id;
    private final String inGameName;
    private final long discordId;
}
