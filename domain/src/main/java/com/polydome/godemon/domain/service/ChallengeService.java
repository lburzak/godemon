package com.polydome.godemon.domain.service;

import com.polydome.godemon.domain.entity.Challenge;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public class ChallengeService {
    private final MatchDetailsEndpoint matchDetailsEndpoint;
    private final ChallengeRepository challengeRepository;
    private final ContributionRepository contributionRepository;
    private final RandomNumberGenerator rng;

    public void synchronizeChallenge(int id) {
        Challenge challenge = challengeRepository.findChallenge(id);

        if (challenge == null)
            return;

        List<MatchDetails> fetchedMatches = challenge.getParticipants().stream()
                .map(Challenger::getInGameId)
                .flatMap(inGameId -> matchDetailsEndpoint.fetchNewerMatches(inGameId, challenge.getGameMode(), challenge.getLastUpdate()).stream())
                .distinct()
                .collect(Collectors.toList());

        var challengeBuilder = challenge.toBuilder();

        Map<Integer, Integer> currentGodPool = challenge.getAvailableGods();
        GodPool godPool = new GodPool(currentGodPool);
        final List<Contribution> contributions = new LinkedList<>();
        Stream<Integer> ownGods;
        PlayerPool playerPool;

        for (final var match : fetchedMatches) {
            playerPool = new PlayerPool(match, challenge);
            ownGods = playerPool.participants().map(PlayerRecord::getGodId);

            playerPool.participants()
                    .map(playerRecord -> playerRecordToContribution(playerRecord, match.getId()))
                    .forEach(contributions::add);

            if (ownGods.allMatch(godPool::contains)) {
                godPool.applyChanges(playerPoolToGodChanges(playerPool));
            }
        }

        challengeBuilder
                .availableGods(godPool.toMap())
                .lastUpdate(Instant.now())
                .build();

        Challenge newChallenge = challengeBuilder.build();

        challengeRepository.updateChallenge(newChallenge);
        contributionRepository.insertAll(challenge.getId(), contributions);
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

    private Stream<Integer> randomize(Stream<Integer> items) {
        List<Integer> available = items.collect(Collectors.toList());

        return Stream.generate(() -> available.remove(rng.getInt(0, available.size())));
    }

    private Stream<GodPool.Change> playerPoolToGodChanges(PlayerPool playerPool) {
        Optional<PlayerRecord> anyParticipant = playerPool.participants().findAny();
        if (anyParticipant.isPresent()) {
            boolean isWin = anyParticipant.get().getWinStatus() == PlayerRecord.WinStatus.WINNER;

            if (isWin) {
                Stream<Integer> opponentGods = playerPool
                        .opponents(true)
                        .map(PlayerRecord::getGodId);

                return randomize(opponentGods)
                        .limit(playerPool.participants().count())
                        .map(godId -> new GodPool.Change(GodPool.ChangeType.GRANT, godId));
            } else {
                return playerPool.participants()
                        .map(PlayerRecord::getGodId)
                        .map(godId -> new GodPool.Change(GodPool.ChangeType.REVOKE, godId));
            }
        } else {
            return Stream.of();
        }
    }
}
