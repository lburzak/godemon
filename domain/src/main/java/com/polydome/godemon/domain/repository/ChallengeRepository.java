package com.polydome.godemon.domain.repository;

import com.polydome.godemon.domain.entity.Challenge;

import java.util.Map;

public interface ChallengeRepository {
    Challenge findChallengeByChallengerId(long Id);
    void createChallenge(long challengerId, Map<Integer, Integer> availableGods);
    void updateChallenge(long challengerId, Challenge newChallenge);
}
