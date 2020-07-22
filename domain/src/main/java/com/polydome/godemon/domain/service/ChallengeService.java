package com.polydome.godemon.domain.service;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.ChallengeStage;
import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.entity.Contribution;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import com.polydome.godemon.domain.repository.ContributionRepository;
import com.polydome.godemon.domain.service.matchdetails.MatchDetails;
import com.polydome.godemon.domain.service.matchdetails.MatchDetailsEndpoint;
import com.polydome.godemon.domain.service.matchdetails.PlayerRecord;
import com.polydome.godemon.domain.usecase.GodPool;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public class ChallengeService {
    private final MatchDetailsEndpoint matchDetailsEndpoint;
    private final ChallengeRepository challengeRepository;
    private final ContributionRepository contributionRepository;

    public Challenge synchronizeChallenge(int id) {
        Challenge challenge = challengeRepository.findChallenge(id);

        if (challenge == null)
            return null;

        List<MatchDetails> fetchedMatches = challenge.getParticipants().stream()
                .map(Challenger::getInGameId)
                .flatMap(inGameId -> matchDetailsEndpoint.fetchNewerMatches(inGameId, challenge.getGameMode(), challenge.getLastUpdate()).stream())
                .distinct()
                .collect(Collectors.toList());

        var challengeBuilder = challenge.toBuilder();

        Map<Integer, Integer> currentGodPool = challenge.getAvailableGods();
        GodPool godPool = new GodPool(currentGodPool);
        final List<Contribution> contributions = new LinkedList<>();
        SeparatedPlayers players;
        Stream<Integer> ownGods;

        for (final var match : fetchedMatches) {
            if (godPool.distinctCount() > challenge.getParticipants().size()) {
                challengeBuilder.status(ChallengeStage.FAILED);
                break;
            }

            players = matchDetailsToSeparatedPlayers(match, challenge);
            ownGods = players.ownTeam.map(PlayerRecord::getGodId);

            players.ownTeam
                    .map(playerRecord -> playerRecordToContribution(playerRecord, match.getId()))
                    .forEach(contributions::add);

            if (ownGods.allMatch(godPool::contains)) {
                godPool.applyChanges(separatedPlayersToGodPoolChanges(players));
            }
        }

        challengeBuilder
                .availableGods(godPool.toMap())
                .lastUpdate(Instant.now())
                .build();

        Challenge newChallenge = challengeBuilder.build();

        challengeRepository.updateChallenge(newChallenge);
        contributionRepository.insertAll(challenge.getId(), contributions);
        return newChallenge;
    }

    private Contribution playerRecordToContribution(PlayerRecord playerRecord, int matchId) {
        return Contribution.builder()
                .matchId(matchId)
                .playerId(playerRecord.getPlayerId())
                .godId(playerRecord.getGodId())
                .deaths(playerRecord.getDeaths())
                .kills(playerRecord.getKills())
                .win(playerRecord.getWinStatus() == PlayerRecord.WinStatus.WINNER)
                .build();
    }

    @AllArgsConstructor
    private static class SeparatedPlayers {
        public final Stream<PlayerRecord> anyTeam;
        public final Stream<PlayerRecord> ownTeam;
    }

    private List<Integer> dropRandom(List<Integer> ids, long count) {
        while (count > 0) {
            ids.remove(ThreadLocalRandom.current().nextInt(0, ids.size()));
            count--;
        }

        return ids;
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
                return dropRandom(
                            separatedPlayers.anyTeam
                                .filter(player -> player.getWinStatus() == PlayerRecord.WinStatus.LOSER)
                                .map(PlayerRecord::getGodId)
                                .collect(Collectors.toList()),
                                separatedPlayers.ownTeam.count()
                            )
                        .stream()
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
}
