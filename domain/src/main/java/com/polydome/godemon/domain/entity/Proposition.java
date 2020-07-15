package com.polydome.godemon.domain.entity;

import lombok.Data;

@Data
public class Proposition {
    private final long challengerId;
    private final int[] gods;
    private final long messageId;
}
