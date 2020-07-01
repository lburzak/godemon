package com.polydome.godemon.domain.usecase;

import lombok.Data;

@Data
public class ChallengeProposition {
    private final int[] gods;
    private final int rerolls;
}
