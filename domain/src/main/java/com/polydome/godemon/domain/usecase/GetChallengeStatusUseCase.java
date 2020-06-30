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
        try {
            Challenger challenger = challengerRepository.findByDiscordId(discordId);
            Challenge challenge = challengeRepository.findByChallengerId(challenger.getId());
            ChallengeStatus status = new ChallengeStatus(challenge.availableGods.size());

            return new Result(null, status);
        } catch (ChallengerNotFoundException e) {
            return new Result(Error.CHALLENGER_NOT_REGISTERED, null);
        } catch (ChallengeNotFoundException e) {
            return new Result(Error.CHALLENGE_NOT_ACTIVE, null);
        }

    }

}
