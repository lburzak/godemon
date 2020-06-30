package com.polydome.godemon.domain.repository;

import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.usecase.ChallengerNotFoundException;

public interface ChallengerRepository {
    Challenger findByDiscordId(long id) throws ChallengerNotFoundException;
}
