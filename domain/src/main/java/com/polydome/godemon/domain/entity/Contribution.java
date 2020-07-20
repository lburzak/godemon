package com.polydome.godemon.domain.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Contribution {
    private final long playerId;
    private final int godId;
    private final boolean win;
    private final short kills;
    private final short deaths;
}
