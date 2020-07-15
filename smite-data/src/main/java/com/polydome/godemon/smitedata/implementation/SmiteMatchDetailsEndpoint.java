package com.polydome.godemon.smitedata.implementation;

import com.polydome.godemon.domain.entity.GameMode;
import com.polydome.godemon.domain.service.matchdetails.MatchDetails;
import com.polydome.godemon.domain.service.matchdetails.MatchDetailsEndpoint;
import com.polydome.godemon.domain.service.matchdetails.PlayerRecord;
import com.polydome.godemon.smiteapi.client.SmiteApiClient;
import com.polydome.godemon.smiteapi.model.MatchParticipantStats;
import com.polydome.godemon.smiteapi.model.Queue;
import com.polydome.godemon.smiteapi.model.RecentMatch;
import io.reactivex.Observable;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

public class SmiteMatchDetailsEndpoint implements MatchDetailsEndpoint {
    private final SmiteApiClient smiteApiClient;

    public SmiteMatchDetailsEndpoint(SmiteApiClient smiteApiClient) {
        this.smiteApiClient = smiteApiClient;
    }

    private PlayerRecord matchParticipantStatsToPlayerRecord(MatchParticipantStats stats) {
        return new PlayerRecord(
                stats.playerId,
                stats.playerName,
                stats.kills,
                stats.deaths,
                stats.godId,
                stats.winStatus == MatchParticipantStats.WinStatus.Winner ? PlayerRecord.WinStatus.WINNER : PlayerRecord.WinStatus.LOSER
        );
    }

    private MatchDetails participantStatsListToMatchDetails(List<MatchParticipantStats> stats, int matchId) {
        return new MatchDetails(
                matchId,
                (byte) stats.size(),
                stats.stream()
                        .map(this::matchParticipantStatsToPlayerRecord)
                        .toArray(PlayerRecord[]::new)
        );
    }

    private Queue modeToQueue(GameMode mode) {
        return switch(mode) {
            case RANKED_DUEL -> Queue.RANKED_DUEL;
            default -> throw new UnsupportedOperationException("Unable to convert mode " + mode.name() + " to queue");
        };
    }

    @Override
    public List<MatchDetails> fetchNewerMatches(int playerId, GameMode mode, Instant instant) {
        return smiteApiClient.getMatchHistory(playerId)
                .flatMapObservable(Observable::fromIterable)
                .takeWhile(match -> match.getDate().isAfter(instant))
                .filter(match -> match.getQueue() == modeToQueue(mode))
                .map(RecentMatch::getId)
                .flatMapMaybe(smiteApiClient::getMatchDetails)
                .map((List<MatchParticipantStats> stats) -> participantStatsListToMatchDetails(stats, stats.get(0).matchId))
                .collectInto((List<MatchDetails>) new LinkedList<MatchDetails>(), List::add)
                .blockingGet();
    }
}
