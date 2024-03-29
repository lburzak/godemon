package com.polydome.godemon.domain.usecase;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.ChallengeStage;
import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import com.polydome.godemon.domain.repository.ChallengerRepository;
import com.polydome.godemon.domain.repository.PropositionRepository;
import lombok.Data;

import java.util.Map;

public class GetAvailableGodsUseCase {
    private final ChallengeRepository challengeRepository;
    private final PropositionRepository propositionRepository;
    private final ChallengerRepository challengerRepository;

    public GetAvailableGodsUseCase(ChallengeRepository challengeRepository, PropositionRepository propositionRepository, ChallengerRepository challengerRepository) {
        this.challengeRepository = challengeRepository;
        this.propositionRepository = propositionRepository;
        this.challengerRepository = challengerRepository;
    }

    public enum Error {
        CHALLENGER_NOT_REGISTERED,
        CHALLENGE_NOT_ACTIVE,
        CHALLENGE_IN_PROPOSITION_STAGE
    }

    @Data
    public class Result {
        private final Error error;
        private final Map<Integer, Integer> godsToUsesLeft;
    }

    public Result execute(long challengerId, int challengeId) {
        Challenger challenger = challengerRepository.findChallengerById(challengerId);

        if (challenger == null)
            return new Result(Error.CHALLENGER_NOT_REGISTERED, null);

        Challenge challenge = challengeRepository.findChallenge(challengeId);

        if (challenge == null)
            return new Result(Error.CHALLENGE_NOT_ACTIVE, null);

        if (challenge.getStatus() == ChallengeStage.PROPOSED)
            return new Result(Error.CHALLENGE_IN_PROPOSITION_STAGE, null);

        return new Result(null, challenge.getAvailableGods());
    }
}
