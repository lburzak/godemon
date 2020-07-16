package com.polydome.godemon.domain.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Proposition {
    private final long messageId;
    private final int challengeId;
    private final long requesterId;
    private final int[] gods;
}
