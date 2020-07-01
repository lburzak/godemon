package com.polydome.godemon.domain.entity;

import lombok.Data;

@Data
public class Proposition {
    private final String challengerId;
    private final int[] gods;
    private final int rerolls;
    private final long messageId;
}
