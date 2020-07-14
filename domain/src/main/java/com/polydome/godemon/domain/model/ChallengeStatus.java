package com.polydome.godemon.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class ChallengeStatus {
    private final int wins;
    private final int loses;
    private final int godsLeftCount;
    private Map<Integer, Integer> godToUsesLeft;
    private final boolean ended;
}
