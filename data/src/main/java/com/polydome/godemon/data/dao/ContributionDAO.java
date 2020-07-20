package com.polydome.godemon.data.dao;

import com.polydome.godemon.domain.entity.Contribution;
import com.polydome.godemon.domain.repository.ContributionRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class ContributionDAO implements ContributionRepository {
    private final PreparedStatement insertContribution;

    public ContributionDAO(Connection connection) throws SQLException {
        this.insertContribution =
                connection.prepareStatement("INSERT INTO contribution (challenege_id, player_id, god_id, win, kills, deaths) VALUES (?, ?, ?, ?, ?, ?)");
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
}
