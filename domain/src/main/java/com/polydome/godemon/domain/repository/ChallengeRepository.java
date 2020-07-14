package com.polydome.godemon.domain.repository;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.GameMode;

import java.util.Map;

public interface ChallengeRepository {
    Challenge findChallengeByChallengerId(long Id);
    void createChallenge(long challengerId, Map<Integer, Integer> availableGods, GameMode gameMode);
    void updateChallenge(long challengerId, Challenge newChallenge);
}
