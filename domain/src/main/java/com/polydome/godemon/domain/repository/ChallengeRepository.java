package com.polydome.godemon.domain.repository;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.usecase.ChallengeNotFoundException;

public interface ChallengeRepository {
    Challenge findByChallengerId(String Id) throws ChallengeNotFoundException;
}
