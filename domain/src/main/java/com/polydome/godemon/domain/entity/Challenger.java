package com.polydome.godemon.domain.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Challenger {
    private final long id;
    private final String inGameName;
    private final int inGameId;
}
