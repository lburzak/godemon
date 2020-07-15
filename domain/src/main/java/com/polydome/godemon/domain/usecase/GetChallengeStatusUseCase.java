package com.polydome.godemon.domain.usecase;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.model.ChallengeStatus;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import com.polydome.godemon.domain.repository.ChallengerRepository;
import com.polydome.godemon.domain.service.matchdetails.MatchDetails;
import com.polydome.godemon.domain.service.matchdetails.MatchDetailsEndpoint;
import com.polydome.godemon.domain.service.matchdetails.PlayerRecord;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class GetChallengeStatusUseCase {
    private final ChallengerRepository challengerRepository;
    private final ChallengeRepository challengeRepository;
    private final MatchDetailsEndpoint matchDetailsEndpoint;

    public GetChallengeStatusUseCase(ChallengerRepository challengerRepository, ChallengeRepository challengeRepository, MatchDetailsEndpoint matchDetailsEndpoint) {
        this.challengerRepository = challengerRepository;
        this.challengeRepository = challengeRepository;
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

    @AllArgsConstructor
    private static class SeparatedPlayers {
        public final Stream<PlayerRecord> anyTeam;
        public final Stream<PlayerRecord> ownTeam;
    }

    private SeparatedPlayers matchDetailsToSeparatedPlayers(MatchDetails matchDetails, Challenge challenge) {
        Stream<PlayerRecord> players = Arrays.stream(matchDetails.getPlayers());
        Stream<PlayerRecord> participants = players
                .filter(player -> challenge.getParticipants().stream()
                        .anyMatch(challenger -> challenger.getInGameId() == player.getPlayerId())
                );

        return new SeparatedPlayers(players, participants);
    }

    private Stream<GodPool.Change> separatedPlayersToGodPoolChanges(SeparatedPlayers separatedPlayers) {
        Optional<PlayerRecord> anyParticipant = separatedPlayers.ownTeam.findAny();
        if (anyParticipant.isPresent()) {
            boolean isWin = anyParticipant.get().getWinStatus() == PlayerRecord.WinStatus.WINNER;

            if (isWin) {
                return separatedPlayers.anyTeam
                        .filter(player -> player.getWinStatus() == PlayerRecord.WinStatus.LOSER)
                        .map(PlayerRecord::getGodId)
                        .map(godId -> new GodPool.Change(GodPool.ChangeType.GRANT, godId));
            } else {
                return separatedPlayers.ownTeam
                        .map(PlayerRecord::getGodId)
                        .map(godId -> new GodPool.Change(GodPool.ChangeType.REVOKE, godId));
            }
        } else {
            return Stream.of();
        }
    }

    public Result execute(long discordId) {
        Challenger challenger = challengerRepository.findByDiscordId(discordId);
        if (challenger == null) {
            return new Result(Error.CHALLENGER_NOT_REGISTERED, null);
        }

        Challenge challenge = challengeRepository.findChallengeByChallengerId(challenger.getId());
        if (challenge == null)
            return new Result(Error.CHALLENGE_NOT_ACTIVE, null);

        List<MatchDetails> fetchedMatches = matchDetailsEndpoint
                .fetchNewerMatches(challenger.getInGameId(), challenge.getGameMode(), challenge.getLastUpdate());

        var challengeBuilder = challenge.toBuilder();

        Map<Integer, Integer> currentGodPool = challenge.getAvailableGods();
        GodPool godPool = new GodPool(currentGodPool);
        SeparatedPlayers players;
        Stream<Integer> ownGods;

        for (final var match : fetchedMatches) {
            if (godPool.distinctCount() > challenge.getParticipants().size()) {
                challengeBuilder.status(com.polydome.godemon.domain.entity.ChallengeStatus.FAILED);
                break;
            }

            players = matchDetailsToSeparatedPlayers(match, challenge);
            ownGods = players.ownTeam.map(PlayerRecord::getGodId);

            if (ownGods.allMatch(godPool::contains)) {
                godPool.applyChanges(separatedPlayersToGodPoolChanges(players));
            }
        }

        challengeBuilder
            .availableGods(godPool.toMap())
            .lastUpdate(Instant.now())
            .build();

        Challenge newChallenge = challengeBuilder.build();

        challengeRepository.updateChallenge(challenger.getId(), newChallenge);

        ChallengeStatus status = ChallengeStatus.builder()
                .ended(newChallenge.getStatus() == com.polydome.godemon.domain.entity.ChallengeStatus.FAILED)
                .godToUsesLeft(godPool.toMap())
                .godsLeftCount(godPool.distinctCount())
                .wins(0)
                .loses(0)
                .build();

        return new Result(null, status);
    }

}
