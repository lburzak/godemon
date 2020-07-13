package com.polydome.godemon.domain.usecase;

import com.polydome.godemon.domain.repository.ChallengerRepository;
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
        if (challengerRepository.findByDiscordId(discordId) != null)
            return new Result(Error.CHALLENGER_ALREADY_REGISTERED, inGameName);

        Integer inGameId = playerEndpoint.fetchPlayerId(inGameName);

        if (inGameId == null)
            return new Result(Error.PLAYER_NOT_EXISTS, null);

        challengerRepository.insert(discordId, inGameName, inGameId);
        return new Result(null, inGameName);
    }
}
