package com.polydome.godemon.domain.usecase;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.ChallengeStage;
import com.polydome.godemon.domain.entity.GameMode;
import com.polydome.godemon.domain.exception.AuthenticationException;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import com.polydome.godemon.domain.repository.ChallengerRepository;
import com.polydome.godemon.domain.repository.exception.NoSuchEntityException;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class StartChallengeUseCase {
    private final ChallengerRepository challengerRepository;
    private final ChallengeRepository challengeRepository;

    public int execute(long discordId, GameMode gameMode) {
        try {
            challengerRepository.findChallengerById(discordId);
        } catch (NoSuchEntityException e) {
            throw new AuthenticationException("Challenger not registered");
        }

        Challenge createdChallenge = challengeRepository.createChallenge(
            Challenge.builder()
                .availableGods(Collections.emptyMap())
                .gameMode(gameMode)
                .lastUpdate(Instant.now())
                .participants(List.of())
                .status(ChallengeStage.PROPOSED)
                .build()
        );

        return createdChallenge.getId();
    }
}
