package com.polydome.godemon.domain.usecase;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.entity.Proposition;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import com.polydome.godemon.domain.repository.ChallengerRepository;
import com.polydome.godemon.domain.repository.PropositionRepository;
import lombok.Data;

import static com.polydome.godemon.domain.usecase.AcceptChallengeUseCase.Error.*;

public class AcceptChallengeUseCase {
    private final ChallengerRepository challengerRepository;
    private final ChallengeRepository challengeRepository;
    private final PropositionRepository propositionRepository;

    public AcceptChallengeUseCase(ChallengerRepository challengerRepository, ChallengeRepository challengeRepository, PropositionRepository propositionRepository) {
        this.challengerRepository = challengerRepository;
        this.challengeRepository = challengeRepository;
        this.propositionRepository = propositionRepository;
    }

    public enum Error {
        CHALLENGER_NOT_REGISTERED,
        CHALLENGE_ALREADY_ACTIVE,
        CHALLENGER_HAS_NO_PROPOSITION
    }

    @Data
    public static class Result {
        private final Error error;
        private final int firstGodId;
    }

    public Result execute(long discordId, int choice) {
        Challenger challenger = challengerRepository.findByDiscordId(discordId);
        if (challenger == null)
            return new Result(CHALLENGER_NOT_REGISTERED, 0);

        Proposition proposition = propositionRepository.findByChallengerId(challenger.getId());
        if (proposition == null)
            return new Result(CHALLENGER_HAS_NO_PROPOSITION, 0);

        Challenge challenge = challengeRepository.findByChallengerId(challenger.getId());

        if (challenge != null && challenge.getAvailableGods().size() > 0)
            return new Result(CHALLENGE_ALREADY_ACTIVE, 0);

        challenge.getAvailableGods().put(proposition.getGods()[choice], 1);

        challengeRepository.update(challenger.getId(), challenge);

        return new Result(null, challenge.getAvailableGods().keySet().iterator().next());
    }
}
