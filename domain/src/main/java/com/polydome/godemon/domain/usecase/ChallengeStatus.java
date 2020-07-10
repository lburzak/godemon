package com.polydome.godemon.domain.usecase;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder(toBuilder = true)
public class ChallengeStatus {
    private final int wins;
    private final int loses;
    private final int godsLeftCount;
    private Map<Integer, Integer> godToUsesLeft;
    private final boolean ended;
}
