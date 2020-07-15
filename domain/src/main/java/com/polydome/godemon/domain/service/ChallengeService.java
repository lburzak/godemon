package com.polydome.godemon.domain.service;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.model.ChallengeStatus;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import com.polydome.godemon.domain.service.matchdetails.MatchDetails;
import com.polydome.godemon.domain.service.matchdetails.MatchDetailsEndpoint;
import com.polydome.godemon.domain.service.matchdetails.PlayerRecord;
import com.polydome.godemon.domain.usecase.GetChallengeStatusUseCase;
import com.polydome.godemon.domain.usecase.GodPool;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChallengeService {
    private final MatchDetailsEndpoint matchDetailsEndpoint;
    private final ChallengeRepository challengeRepository;

    public ChallengeService(MatchDetailsEndpoint matchDetailsEndpoint, ChallengeRepository challengeRepository) {
        this.matchDetailsEndpoint = matchDetailsEndpoint;
        this.challengeRepository = challengeRepository;
    }

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

        challengeRepository.updateChallenge(newChallenge);
        return newChallenge;
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
}
