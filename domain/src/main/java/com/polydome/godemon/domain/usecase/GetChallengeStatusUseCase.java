package com.polydome.godemon.domain.usecase;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.entity.Match;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import com.polydome.godemon.domain.repository.ChallengerRepository;
import com.polydome.godemon.domain.repository.MatchRepository;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

public class GetChallengeStatusUseCase {
    private final ChallengerRepository challengerRepository;
    private final ChallengeRepository challengeRepository;
    private final MatchRepository matchRepository;

    public GetChallengeStatusUseCase(ChallengerRepository challengerRepository, ChallengeRepository challengeRepository, MatchRepository matchRepository) {
        this.challengerRepository = challengerRepository;
        this.challengeRepository = challengeRepository;
        this.matchRepository = matchRepository;
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

    private ChallengeStatus matchToStatusReducer(ChallengeStatus status, Match match) {
        var statusBuilder = status.toBuilder();

        if (status.isEnded())
            return statusBuilder.build();

        if (status.getGodToUsesLeft().containsKey(match.getOwnGod()))
            return statusBuilder.build();

        Map<Integer, Integer> godToUsesLeft = new HashMap<>(Map.copyOf(status.getGodToUsesLeft()));

        if (match.isWin()) {
            statusBuilder.wins(status.getWins() + 1);

            Integer currentLeft = godToUsesLeft.getOrDefault(match.getOpponentGod(), 0);
            godToUsesLeft.put(match.getOpponentGod(), currentLeft + 1);
        } else {
            statusBuilder.loses(status.getLoses() + 1);

            int currentLeft = godToUsesLeft.getOrDefault(match.getOwnGod(), 0);
            int newLeft = currentLeft - 1;

            if (newLeft < 1) {
                godToUsesLeft.remove(match.getOwnGod());
                if (godToUsesLeft.size() < 1)
                    return statusBuilder.ended(true).build();
            } else {
                godToUsesLeft.put(match.getOwnGod(), newLeft);
            }
        }

        return statusBuilder
                .godToUsesLeft(godToUsesLeft)
                .godsLeftCount(godToUsesLeft.size())
                .build();
    }

    private ChallengeStatus statusCombiner(ChallengeStatus combined, ChallengeStatus intermediate) {
        throw new UnsupportedOperationException("Parallel stream reduction is not supported");
    }

    public Result execute(long discordId) {
        Challenger challenger = challengerRepository.findByDiscordId(discordId);
        if (challenger == null) {
            return new Result(Error.CHALLENGER_NOT_REGISTERED, null);
        }

        Challenge challenge = challengeRepository.findChallengeByChallengerId(challenger.getId());
        if (challenge == null)
            return new Result(Error.CHALLENGE_NOT_ACTIVE, null);

        ChallengeStatus initialStatus =
                new ChallengeStatus(0, 0, challenge.getAvailableGods().size(), challenge.getAvailableGods(), false);

        ChallengeStatus status = matchRepository.findMatchesByChallenge(challenge.getId())
                .reduce(initialStatus,
                        this::matchToStatusReducer,
                        this::statusCombiner);
        return new Result(null, status);
    }

}
