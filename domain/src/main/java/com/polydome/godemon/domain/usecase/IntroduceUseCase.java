package com.polydome.godemon.domain.usecase;

import com.polydome.godemon.domain.repository.ChallengerRepository;
import lombok.Data;

public class IntroduceUseCase {
    private final ChallengerRepository challengerRepository;

    public IntroduceUseCase(ChallengerRepository challengerRepository) {
        this.challengerRepository = challengerRepository;
    }

    @Data
    public static class Result {
        private final Error error;
        private final String newName;
    }

    public enum Error {
        CHALLENGER_ALREADY_REGISTERED
    }

    public Result execute(long discordId, String inGameName) {
        if (challengerRepository.findByDiscordId(discordId) != null)
            return new Result(Error.CHALLENGER_ALREADY_REGISTERED, inGameName);

        challengerRepository.insert(discordId, inGameName);
        return new Result(null, inGameName);
    }
}
