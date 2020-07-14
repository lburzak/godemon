package com.polydome.godemon.smiteapi.implementation;

import com.polydome.godemon.data.repository.MatchDetails;
import com.polydome.godemon.data.repository.MatchDetailsEndpoint;
import com.polydome.godemon.data.repository.PlayerRecord;
import com.polydome.godemon.smiteapi.client.SmiteApiClient;
import com.polydome.godemon.smiteapi.model.MatchParticipantStats;
import com.polydome.godemon.smiteapi.model.Queue;
import com.polydome.godemon.smiteapi.model.RecentMatch;
import io.reactivex.Observable;
import io.reactivex.Single;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MatchDetailsEndpointImpl implements MatchDetailsEndpoint {
    private final SmiteApiClient smiteApiClient;

    public MatchDetailsEndpointImpl(SmiteApiClient smiteApiClient) {
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

    private MatchDetails participantStatsListToMatchDetails(List<MatchParticipantStats> stats) {
        return new MatchDetails(
                (byte) stats.size(),
                stats.stream()
                        .map(this::matchParticipantStatsToPlayerRecord)
                        .toArray(PlayerRecord[]::new)
        );
    }

    private Queue modeToQueue(MatchDetails.Mode mode) {
        return switch(mode) {
            case RANKED_DUEL -> Queue.RANKED_DUEL;
            default -> throw new UnsupportedOperationException("Unable to convert mode " + mode.name() + " to queue");
        };
    }

    @Override
    public Single<List<MatchDetails>> fetchNewerMatches(int playerId, MatchDetails.Mode mode, Date date) {
        return smiteApiClient.getMatchHistory(playerId)
                .flatMapObservable(Observable::fromIterable)
                .takeWhile(match -> match.getDate().isAfter(date.toInstant()))
                .filter(match -> match.getQueue() == modeToQueue(mode))
                .map(RecentMatch::getId)
                .flatMapMaybe(smiteApiClient::getMatchDetails)
                .map(this::participantStatsListToMatchDetails)
                .collectInto(new LinkedList<>(), List::add);
    }
}
