package com.polydome.godemon.domain.repository;

import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.repository.exception.CRUDException;
import com.polydome.godemon.domain.repository.exception.NoSuchEntityException;

public interface ChallengerRepository {
    Challenger findChallengerById(long id) throws NoSuchEntityException;
    boolean existsChallenger(long id) throws CRUDException;
    void createChallenger(long discordId, String inGameName, int inGameId);
}
