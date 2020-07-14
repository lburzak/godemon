package com.polydome.godemon.domain.usecase;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.entity.Match;
import com.polydome.godemon.domain.model.ChallengeStatus;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import com.polydome.godemon.domain.repository.ChallengerRepository;
import com.polydome.godemon.domain.repository.MatchRepository;
import com.polydome.godemon.domain.service.matchdetails.MatchDetails;
import com.polydome.godemon.domain.service.matchdetails.MatchDetailsEndpoint;
import com.polydome.godemon.domain.service.matchdetails.PlayerRecord;
import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class GetChallengeStatusUseCase {
    private final ChallengerRepository challengerRepository;
    private final ChallengeRepository challengeRepository;
    private final MatchRepository matchRepository;
    private final MatchDetailsEndpoint matchDetailsEndpoint;

    public GetChallengeStatusUseCase(ChallengerRepository challengerRepository, ChallengeRepository challengeRepository, MatchRepository matchRepository, MatchDetailsEndpoint matchDetailsEndpoint) {
        this.challengerRepository = challengerRepository;
        this.challengeRepository = challengeRepository;
        this.matchRepository = matchRepository;
        this.matchDetailsEndpoint = matchDetailsEndpoint;
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

    private Match matchDetailsToMatch(MatchDetails matchDetails, int ownPlayerId) {
        int ownIndex = matchDetails.getPlayers()[0].getPlayerId() == ownPlayerId ? 0 : 1;
        int opponentIndex = ownIndex == 0 ? 1 : 0;
        PlayerRecord ownRecord = matchDetails.getPlayers()[ownIndex];
        PlayerRecord opponentRecord = matchDetails.getPlayers()[opponentIndex];

        return new Match(
                ownRecord.getGodId(),
                opponentRecord.getGodId(),
                ownRecord.getKills(),
                ownRecord.getDeaths(),
                ownRecord.getWinStatus() == PlayerRecord.WinStatus.WINNER
        );
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

        matchDetailsEndpoint
                .fetchNewerMatches(challenger.getInGameId(), challenge.getGameMode(), challenge.getLastUpdate())
                .stream()
                // TODO: Remove all participants but player and one enemy
                .filter(matchDetails -> matchDetails.getParticipantsCount() == 2)
                .map(matchDetails -> matchDetailsToMatch(matchDetails, challenger.getInGameId()))
                .forEach(matchRepository::createMatch);

        var touchedChallenge = challenge.toBuilder()
                .lastUpdate(Instant.now())
                .build();

        challengeRepository.updateChallenge(challenger.getId(), touchedChallenge);

        ChallengeStatus status = matchRepository.findMatchesByChallenge(challenge.getId())
                .reduce(initialStatus,
                        this::matchToStatusReducer,
                        this::statusCombiner);

        return new Result(null, status);
    }

}
