package com.polydome.godemon.domain.usecase;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import com.polydome.godemon.domain.repository.ChallengerRepository;
import lombok.Data;

public class GetChallengeStatusUseCase {
    private final ChallengerRepository challengerRepository;
    private final ChallengeRepository challengeRepository;

    public GetChallengeStatusUseCase(ChallengerRepository challengerRepository, ChallengeRepository challengeRepository) {
        this.challengerRepository = challengerRepository;
        this.challengeRepository = challengeRepository;
    }

    public enum Error {
        CHALLENGER_NOT_REGISTERED,
        CHALLENGE_NOT_ACTIVE
    }

    @Data
    public static class Result {
        public final Error error;
        public final ChallengeStatus status;
    }

    public Result execute(long discordId) {
        Challenger challenger = challengerRepository.findByDiscordId(discordId);
        if (challenger == null) {
            return new Result(Error.CHALLENGER_NOT_REGISTERED, null);
        }

        Challenge challenge = challengeRepository.findByChallengerId(challenger.getId());
        if (challenge == null)
            return new Result(Error.CHALLENGE_NOT_ACTIVE, null);

        ChallengeStatus status = new ChallengeStatus(challenge.availableGods.size());
        return new Result(null, status);
    }

}
