package com.polydome.godemon.data.dao;

import com.polydome.godemon.domain.entity.Contribution;
import com.polydome.godemon.domain.repository.ContributionRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class ContributionDAO implements ContributionRepository {
    private final PreparedStatement insertContribution;
    private final PreparedStatement selectContributions;

    public ContributionDAO(Connection connection) throws SQLException {
        this.insertContribution =
                connection.prepareStatement("INSERT INTO contribution (match_id, participant_id, challenge_id, god_id, win, kills, deaths) VALUES (?, ?, ?, ?, ?, ?, ?)");
        this.selectContributions =
                connection.prepareStatement("SELECT * FROM contribution WHERE challenge_id = ?");
    }

    @Override
    public void insertAll(int challengeId, List<Contribution> contributions) {
        try {
            insertContribution.setInt(1, challengeId);

            for (final var contribution : contributions) {
                insertContribution.setLong(2, contribution.getPlayerId());
                insertContribution.setInt(3, contribution.getGodId());
                insertContribution.setBoolean(4, contribution.isWin());
                insertContribution.setShort(5, contribution.getKills());
                insertContribution.setShort(6, contribution.getDeaths());
                insertContribution.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Contribution> findContributionsByChallenge(int challengeId) {
        try {
            selectContributions.setInt(1, challengeId);
            ResultSet resultSet = selectContributions.executeQuery();

            List<Contribution> contributions = new LinkedList<>();
            while (resultSet.next()) {
                contributions.add(
                        Contribution.builder()
                            .matchId(resultSet.getInt("match_id"))
                            .playerId(resultSet.getLong("participant_id"))
                            .godId(resultSet.getInt("god_id"))
                            .win(resultSet.getBoolean("win"))
                            .deaths(resultSet.getShort("deaths"))
                            .kills(resultSet.getShort("kills"))
                            .build()
                );
            }

            return contributions;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
