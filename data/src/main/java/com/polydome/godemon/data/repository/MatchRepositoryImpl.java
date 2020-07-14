package com.polydome.godemon.data.repository;

import com.polydome.godemon.data.dao.ChallengeDAO;
import com.polydome.godemon.domain.entity.Match;
import com.polydome.godemon.domain.repository.MatchRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;
import java.util.stream.Stream;

@AllArgsConstructor
public class MatchRepositoryImpl implements MatchRepository {
    private final MatchDetailsEndpoint matchDetailsEndpoint;
    private final ChallengeDAO challengeDAO;

    private Match matchDetailsToMatch(MatchDetails matchDetails, int ownPlayerId) {
        int ownIndex = matchDetails.getPlayers()[0].getPlayerId() == ownPlayerId ? 0 : 1;
        int opponentIndex = ownIndex == 0 ? 1 : 0;
        PlayerRecord ownRecord = matchDetails.getPlayers()[ownIndex];
        PlayerRecord opponentRecord = matchDetails.getPlayers()[opponentIndex];

        return new Match(
                ownRecord.getGodId(),
                opponentRecord.getGodId(),
                ownRecord.getKills(),
                ownRecord.getDeaths(),
                ownRecord.getWinStatus() == PlayerRecord.WinStatus.WINNER
        );
    }

    @Data
    public static class ChallengeUpdateData {
        private final int playerId;
        private final Timestamp lastUpdated;
    }

    @Override
    public Stream<Match> findMatchesByChallenge(int challengeId) {
        ChallengeUpdateData updateData = challengeDAO.findChallengeUpdateData(challengeId)
                .blockingGet();

        return matchDetailsEndpoint
                .fetchNewerMatches(updateData.playerId, MatchDetails.Mode.RANKED_DUEL, updateData.lastUpdated)
                .blockingGet()
                .stream()
                .filter(matchDetails -> matchDetails.getParticipantsCount() == 2)
                .map(matchDetails -> matchDetailsToMatch(matchDetails, updateData.playerId));
    }
}
