package com.polydome.godemon.domain.entity;

import lombok.Data;

@Data
public class Proposition {
    private final long challengerId;
    private final int[] gods;
    private final int rerolls;
    private final long messageId;
}
