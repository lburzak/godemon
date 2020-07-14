package com.polydome.godemon.domain.factory;

import com.polydome.godemon.domain.entity.Challenge;

import java.util.Map;

public interface ChallengeFactory {
    Challenge create(final int challengeId, final Map<Integer, Integer> availableGods, final int gameModeId);
}
