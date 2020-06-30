package com.polydome.godemon.domain.usecase;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import com.polydome.godemon.domain.repository.ChallengerRepository;
import lombok.Data;

import static com.polydome.godemon.domain.usecase.AcceptChallengeUseCase.Error.CHALLENGER_NOT_REGISTERED;
import static com.polydome.godemon.domain.usecase.AcceptChallengeUseCase.Error.CHALLENGE_ALREADY_ACTIVE;

public class AcceptChallengeUseCase {
    private final ChallengerRepository challengerRepository;
    private final ChallengeRepository challengeRepository;

    public AcceptChallengeUseCase(ChallengerRepository challengerRepository, ChallengeRepository challengeRepository) {
        this.challengerRepository = challengerRepository;
        this.challengeRepository = challengeRepository;
    }

    public enum Error {
        CHALLENGER_NOT_REGISTERED,
        CHALLENGE_ALREADY_ACTIVE
    }

    @Data
    public static class Result {
        private final Error error;
        private final int firstGodId;
    }

    public Result execute(long discordId) {
        Challenger challenger = challengerRepository.findByDiscordId(discordId);
        if (challenger == null)
            return new Result(CHALLENGER_NOT_REGISTERED, 0);

        Challenge challenge = challengeRepository.findByChallengerId(challenger.getId());

        if (challenge != null && challenge.isActive())
            return new Result(CHALLENGE_ALREADY_ACTIVE, 0);

        challenge.setActive(true);
        challengeRepository.update(challenger.getId(), challenge);

        return new Result(null, challenge.getAvailableGods().keySet().iterator().next());
    }
}
