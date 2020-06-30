package com.polydome.godemon.domain.repository;

import com.polydome.godemon.domain.entity.Challenge;

public interface ChallengeRepository {
    Challenge findByChallengerId(String Id);
}
