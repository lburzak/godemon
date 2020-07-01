package com.polydome.godemon.domain.usecase;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import com.polydome.godemon.domain.repository.ChallengerRepository;
import lombok.Data;
import lombok.NonNull;

import java.util.Map;

import static com.polydome.godemon.domain.usecase.StartChallengeUseCase.Error.CHALLENGER_NOT_REGISTERED;
import static com.polydome.godemon.domain.usecase.StartChallengeUseCase.Error.CHALLENGE_ALREADY_ACTIVE;

public class StartChallengeUseCase {
    private final ChallengerRepository challengerRepository;
    private final ChallengeRepository challengeRepository;
    private final GameRulesProvider gameRulesProvider;
    private final RandomNumberGenerator randomNumberGenerator;

    public StartChallengeUseCase(ChallengerRepository challengerRepository, ChallengeRepository challengeRepository, GameRulesProvider gameRulesProvider, RandomNumberGenerator randomNumberGenerator) {
        this.challengerRepository = challengerRepository;
        this.challengeRepository = challengeRepository;
        this.gameRulesProvider = gameRulesProvider;
        this.randomNumberGenerator = randomNumberGenerator;
    }

    @Data
    public static class Result {
        private final Error error;
        private final ChallengeProposition proposition;
    }

    public enum Error {
        CHALLENGER_NOT_REGISTERED,
        CHALLENGE_ALREADY_ACTIVE
    }

    public Result execute(long discordId) {
        Challenger challenger = challengerRepository.findByDiscordId(discordId);
        if (challenger == null)
            return new Result(CHALLENGER_NOT_REGISTERED, null);

        Challenge challenge = challengeRepository.findByChallengerId(challenger.getId());
        if (challenge != null && challenge.isActive())
            return new Result(CHALLENGE_ALREADY_ACTIVE, null);

        int firstGodId = randomNumberGenerator.getInt(0, gameRulesProvider.getGodsCount() - 1);

        challengeRepository.insert(challenger.getId(), Map.of(firstGodId, 1), false);

        ChallengeProposition challengeProposition = new ChallengeProposition(
            getRandomGods(gameRulesProvider.getChallengeProposedGodsCount()),
            gameRulesProvider.getBaseRerolls()
        );

        return new Result(null, challengeProposition);
    }

    @NonNull
    private int[] getRandomGods(int count) {
        int[] gods = new int[count];
        for (int i = 0; i < count; i++)
            gods[i] = randomNumberGenerator.getInt(0, gameRulesProvider.getGodsCount());

        return gods;
    }
}
