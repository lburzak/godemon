package com.polydome.godemon.domain.entity;

import lombok.Data;
import lombok.NonNull;

import java.util.Map;

@Data
public class Challenge {
    @NonNull public Map<Integer, Integer> availableGods;
}
