package com.polydome.godemon.domain.repository;

import com.polydome.godemon.domain.entity.Challenger;

public interface ChallengerRepository {
    Challenger findByDiscordId(long id);
    Challenger insert(long discordId, String inGameName);
}
