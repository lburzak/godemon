package com.polydome.godemon.domain.entity;

import lombok.Data;

@Data
public class Proposition {
    private final long messageId;
    private final int challengeId;
    private final long requesterId;
    private final int[] gods;
}
