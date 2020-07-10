package com.polydome.godemon.domain.entity;

import lombok.Data;
import lombok.NonNull;

import java.util.Map;

@Data
public class Challenge {
    private final int id;
    @NonNull private final Map<Integer, Integer> availableGods;
}
