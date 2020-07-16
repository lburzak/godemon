package com.polydome.godemon.domain.usecase;

import com.polydome.godemon.domain.exception.ActionForbiddenException;
import com.polydome.godemon.domain.repository.ChallengerRepository;
import com.polydome.godemon.domain.repository.exception.NoSuchEntityException;
import com.polydome.godemon.domain.service.PlayerEndpoint;
import lombok.Data;

public class IntroduceUseCase {
    private final ChallengerRepository challengerRepository;
    private final PlayerEndpoint playerEndpoint;

    public IntroduceUseCase(ChallengerRepository challengerRepository, PlayerEndpoint playerEndpoint) {
        this.challengerRepository = challengerRepository;
        this.playerEndpoint = playerEndpoint;
    }

    @Data
    public static class Result {
        private final Error error;
        private final String newName;
    }

    public enum Error {
        CHALLENGER_ALREADY_REGISTERED,
        PLAYER_NOT_EXISTS
    }

    public Result execute(long discordId, String inGameName) {
        try {
            // TODO: Should check for existing instead
            challengerRepository.findByDiscordId(discordId);
        } catch (NoSuchEntityException e) {
            Integer inGameId = playerEndpoint.fetchPlayerId(inGameName);

            if (inGameId == null)
                return new Result(Error.PLAYER_NOT_EXISTS, null);

            challengerRepository.insert(discordId, inGameName, inGameId);
            return new Result(null, inGameName);
        }

        throw new ActionForbiddenException("User already registered");
    }
}
