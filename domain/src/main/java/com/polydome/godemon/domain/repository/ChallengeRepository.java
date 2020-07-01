package com.polydome.godemon.domain.repository;

import com.polydome.godemon.domain.entity.Challenge;

import java.util.Map;

public interface ChallengeRepository {
    Challenge findByChallengerId(String Id);
    void insert(String challengerId, Map<Integer, Integer> availableGods);
    void update(String challengerId, Challenge newChallenge);
}
