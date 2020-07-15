package com.polydome.godemon.domain.usecase;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.model.ChallengeStatus;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import com.polydome.godemon.domain.repository.ChallengerRepository;
import com.polydome.godemon.domain.service.ChallengeService;
import lombok.Data;

public class GetChallengeStatusUseCase {
    private final ChallengerRepository challengerRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeService challengeService;

    public GetChallengeStatusUseCase(ChallengerRepository challengerRepository, ChallengeRepository challengeRepository, ChallengeService challengeService) {
        this.challengerRepository = challengerRepository;
        this.challengeRepository = challengeRepository;
        this.challengeService = challengeService;
    }

    public enum Error {
        CHALLENGER_NOT_REGISTERED,
        CHALLENGE_NOT_ACTIVE
    }

    @Data
    public static class Result {
        public final GetChallengeStatusUseCase.Error error;
        public final ChallengeStatus status;
    }

    public Result execute(long discordId) {
        Challenger challenger = challengerRepository.findByDiscordId(discordId);
        if (challenger == null) {
            return new Result(Error.CHALLENGER_NOT_REGISTERED, null);
        }

        Challenge challenge = challengeRepository.findChallengeByChallengerId(challenger.getId());
        if (challenge == null)
            return new Result(Error.CHALLENGE_NOT_ACTIVE, null);

        challenge = challengeService.synchronizeChallenge(challenge.getId());

        ChallengeStatus status = ChallengeStatus.builder()
                .ended(challenge.getStatus() == com.polydome.godemon.domain.entity.ChallengeStatus.FAILED)
                .godToUsesLeft(challenge.getAvailableGods())
                .godsLeftCount(challenge.getAvailableGods().size())
                .wins(0)
                .loses(0)
                .build();

        return new Result(null, status);
    }

}
