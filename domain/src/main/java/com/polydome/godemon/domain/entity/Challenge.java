package com.polydome.godemon.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.time.Instant;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Challenge {
    private final int id;
    @NonNull private final Map<Integer, Integer> availableGods;
    private final GameMode gameMode;
    private final Instant lastUpdate;
}
