package com.polydome.godemon.domain.repository;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.repository.exception.CRUDException;

import java.util.List;

public interface ChallengeRepository {
    Challenge findChallenge(int id) throws CRUDException;
    List<Challenge> findChallengesByParticipant(long participantId) throws CRUDException;
    void updateChallenge(Challenge challenge) throws CRUDException;
    void createChallenge(Challenge challenge) throws CRUDException;
    List<Challenge> findAllChallenges() throws CRUDException;
}
