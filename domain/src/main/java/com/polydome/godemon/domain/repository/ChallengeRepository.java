package com.polydome.godemon.domain.repository;

import com.polydome.godemon.domain.entity.Challenge;

public interface ChallengeRepository {
    Challenge findChallenge(int id);
    void updateChallenge(Challenge challenge);
    void createChallenge(Challenge challenge);
}
