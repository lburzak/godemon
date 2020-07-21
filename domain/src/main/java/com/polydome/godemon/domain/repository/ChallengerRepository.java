package com.polydome.godemon.domain.repository;

import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.repository.exception.NoSuchEntityException;

public interface ChallengerRepository {
    Challenger findChallengerById(long id) throws NoSuchEntityException;
    void createChallenger(long discordId, String inGameName, int inGameId);
}
