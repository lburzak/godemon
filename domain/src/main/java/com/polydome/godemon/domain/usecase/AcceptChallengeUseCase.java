package com.polydome.godemon.domain.usecase;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.entity.Proposition;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import com.polydome.godemon.domain.repository.ChallengerRepository;
import com.polydome.godemon.domain.repository.PropositionRepository;
import lombok.Data;

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
        MESSAGE_IS_NOT_PROPOSITION,
        CHALLENGER_IS_NOT_PARTICIPANT
    }

    @Data
    public static class Result {
        private final Error error;
        private final int firstGodId;
    }

    public Result execute(long discordId, long messageId, int godIdChoice) {
        Proposition proposition = propositionRepository.findProposition(messageId);
        if (proposition == null) {
            return new Result(Error.MESSAGE_IS_NOT_PROPOSITION, 0);
        }

        Challenger challenger = challengerRepository.findByDiscordId(discordId);
        if (challenger == null)
            return new Result(Error.CHALLENGER_NOT_REGISTERED, 0);

        Challenge challenge = challengeRepository.findChallenge(proposition.getChallengeId());
        if (challenge == null) {
            throw new IllegalStateException("Interaction with orphan proposition occurred");
        }

        if (challenge.getParticipants().stream().noneMatch(participant -> participant.getId() == discordId)) {
            return new Result(Error.CHALLENGER_IS_NOT_PARTICIPANT, 0);
        }

        GodPool godPool = new GodPool(challenge.getAvailableGods());
        godPool.grantOne(godIdChoice);

        challengeRepository.updateChallenge(challenge.toBuilder()
                .availableGods(godPool.toMap())
                .build()
        );

        propositionRepository.deleteProposition(messageId);

        return new Result(null, godIdChoice);
    }
}
