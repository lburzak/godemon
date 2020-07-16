package com.polydome.godemon.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ChallengeBrief {
    private final int id;
    private final Instant lastUpdate;
}
