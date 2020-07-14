package com.polydome.godemon.data.repository.service;

import com.polydome.godemon.data.repository.model.MatchDetails;
import io.reactivex.Single;

import java.util.Date;
import java.util.List;

public interface MatchDetailsEndpoint {
    Single<List<MatchDetails>> fetchNewerMatches(int playerId, MatchDetails.Mode mode, Date date);
}
