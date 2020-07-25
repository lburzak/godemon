package com.polydome.godemon.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Challenge {
    private final int id;
    private final Map<Integer, Integer> availableGods;
    private final GameMode gameMode;
    private final Instant lastUpdate;
    private final Instant createdAt;
    private final List<Challenger> participants;
    private final ChallengeStage status;
}
