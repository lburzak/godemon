package com.polydome.godemon.domain.entity;

import lombok.Data;

@Data
public class Contribution {
    private final int godId;
    private final boolean win;
    private final int kills;
    private final int deaths;
}
