package com.polydome.godemon.domain.usecase;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import com.polydome.godemon.domain.repository.ChallengerRepository;
import com.polydome.godemon.domain.repository.ChampionRepository;
import com.polydome.godemon.domain.repository.PropositionRepository;
import lombok.Data;

import java.util.Map;

import static com.polydome.godemon.domain.usecase.StartChallengeUseCase.Error.*;

public class StartChallengeUseCase {
    private final ChallengerRepository challengerRepository;
    private final ChallengeRepository challengeRepository;
    private final GameRulesProvider gameRulesProvider;
    private final PropositionRepository propositionRepository;
    private final ChampionRepository championRepository;

    public StartChallengeUseCase(ChallengerRepository challengerRepository, ChallengeRepository challengeRepository, GameRulesProvider gameRulesProvider, PropositionRepository propositionRepository, ChampionRepository championRepository) {
        this.challengerRepository = challengerRepository;
        this.challengeRepository = challengeRepository;
        this.gameRulesProvider = gameRulesProvider;
        this.propositionRepository = propositionRepository;
        this.championRepository = championRepository;
    }

    @Data
    public static class Result {
        private final Error error;
        private final ChallengeProposition proposition;
    }

    public enum Error {
        CHALLENGER_NOT_REGISTERED,
        CHALLENGE_ALREADY_ACTIVE,
        CHALLENGE_ALREADY_PROPOSED
    }

    public Result execute(long discordId, long messageId) {
        Challenger challenger = challengerRepository.findByDiscordId(discordId);
        if (challenger == null)
            return new Result(CHALLENGER_NOT_REGISTERED, null);

        Challenge challenge = challengeRepository.findByChallengerId(challenger.getId());
        if (challenge != null && challenge.getAvailableGods().size() > 0)
            return new Result(CHALLENGE_ALREADY_ACTIVE, null);

        if (propositionRepository.findByChallengerId(challenger.getId()) != null)
            return new Result(CHALLENGE_ALREADY_PROPOSED, null);

        challengeRepository.insert(challenger.getId(), Map.of());

        int[] gods = championRepository.getRandomIds(gameRulesProvider.getChallengeProposedGodsCount());
        int rerolls = gameRulesProvider.getBaseRerolls();

        propositionRepository.insert(challenger.getId(), gods, rerolls, messageId);

        ChallengeProposition challengeProposition = new ChallengeProposition(
            gods,
            rerolls
        );

        return new Result(null, challengeProposition);
    }
}
