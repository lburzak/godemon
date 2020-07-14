package com.polydome.godemon.domain.service.matchdetails;

import com.polydome.godemon.domain.entity.GameMode;

import java.time.Instant;
import java.util.List;

public interface MatchDetailsEndpoint {
    List<MatchDetails> fetchNewerMatches(int playerId, GameMode mode, Instant date);
}
