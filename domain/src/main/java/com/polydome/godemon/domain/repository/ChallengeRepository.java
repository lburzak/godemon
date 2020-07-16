package com.polydome.godemon.domain.repository;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.repository.exception.CRUDException;

public interface ChallengeRepository {
    Challenge findChallenge(int id) throws CRUDException;
    void updateChallenge(Challenge challenge) throws CRUDException;
    void createChallenge(Challenge challenge) throws CRUDException;
}
