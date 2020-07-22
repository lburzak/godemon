package com.polydome.godemon.domain.service;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.service.matchdetails.MatchDetails;
import com.polydome.godemon.domain.service.matchdetails.PlayerRecord;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlayerPool {
    private final MatchDetails matchDetails;
    private final List<Integer> participantsIds;

    public PlayerPool(MatchDetails matchDetails, Challenge challenge) {
        this.matchDetails = matchDetails;
        this.participantsIds = challenge.getParticipants().stream()
                .map(Challenger::getInGameId)
                .collect(Collectors.toList());
    }

    public Stream<PlayerRecord> players() {
        return Arrays.stream(matchDetails.getPlayers());
    }

    public Stream<PlayerRecord> participants() {
        return players().filter(player -> participantsIds.contains(player.getPlayerId()));
    }

    public Stream<PlayerRecord> opponents(boolean win) {
        return win ? losers() : winners();
    }

    public Stream<PlayerRecord> winners() {
        return players()
                .filter(player -> player.getWinStatus() == PlayerRecord.WinStatus.WINNER);
    }

    public Stream<PlayerRecord> losers() {
        return players()
                .filter(player -> player.getWinStatus() == PlayerRecord.WinStatus.LOSER);
    }
}
