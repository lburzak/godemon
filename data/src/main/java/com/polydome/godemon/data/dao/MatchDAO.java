package com.polydome.godemon.data.dao;

import com.polydome.godemon.domain.entity.Match;
import com.polydome.godemon.domain.repository.MatchRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;

public class MatchDAO implements MatchRepository {
    private final PreparedStatement selectMatchesByChallengeId;
    private final PreparedStatement insertMatchStatement;

    public MatchDAO(Connection dbConnection) throws SQLException {
        selectMatchesByChallengeId =
                dbConnection.prepareStatement("SELECT * FROM `match` WHERE challenge_id = ?");
        insertMatchStatement =
                dbConnection.prepareStatement("INSERT INTO `match` (challenge_id, own_god_id, opponent_god_id, win, kills, deaths) VALUES (?, ?, ?, ?, ?, ?)");
    }

    @Override
    public Stream<Match> findMatchesByChallenge(int challengeId) {
        try {
            selectMatchesByChallengeId.setInt(1, challengeId);
            ResultSet row = selectMatchesByChallengeId.executeQuery();

            Stream.Builder<Match> streamBuilder = Stream.builder();

            while (row.next()) {
                streamBuilder.add(new Match(
                        row.getInt("own_god_id"),
                        row.getInt("opponent_god_id"),
                        row.getByte("kills"),
                        row.getByte("deaths"),
                        row.getBoolean("win")
                ));
            }

            return streamBuilder.build();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Stream.of();
    }

    @Override
    public void createMatch(Match match, int challengeId) {
        try {
            insertMatchStatement.setInt(1, challengeId);
            insertMatchStatement.setInt(2, match.getOwnGod());
            insertMatchStatement.setInt(3, match.getOpponentGod());
            insertMatchStatement.setBoolean(4, match.isWin());
            insertMatchStatement.setByte(5, match.getKills());
            insertMatchStatement.setByte(6, match.getDeaths());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
