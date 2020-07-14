package com.polydome.godemon.domain.repository;

import com.polydome.godemon.domain.entity.Match;

import java.util.stream.Stream;

public interface MatchRepository {
    Stream<Match> findMatchesByChallenge(int challengeId);
    void createMatch(Match match);
}
