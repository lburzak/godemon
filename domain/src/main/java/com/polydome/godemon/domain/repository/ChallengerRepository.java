package com.polydome.godemon.domain.repository;

import com.polydome.godemon.domain.entity.Challenger;

public interface ChallengerRepository {
    Challenger findChallengerById(long id);
    void createChallenger(long discordId, String inGameName, int inGameId);
}
