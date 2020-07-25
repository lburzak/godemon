package com.polydome.godemon.data.dao;

import com.polydome.godemon.data.dao.common.BaseDAO;
import com.polydome.godemon.domain.entity.Contribution;
import com.polydome.godemon.domain.repository.ContributionRepository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class ContributionDAO extends BaseDAO implements ContributionRepository {
    public ContributionDAO(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void insertAll(int challengeId, List<Contribution> contributions) {
        try (final var connection = getConnection(); final var insertContribution =
                connection.prepareStatement("INSERT INTO contribution (match_id, participant_id, challenge_id, god_id, win, kills, deaths) VALUES (?, ?, ?, ?, ?, ?, ?)");) {
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
        try (final var connection = getConnection(); final var selectContributions =
                connection.prepareStatement("SELECT * FROM contribution WHERE challenge_id = ?");) {

            selectContributions.setInt(1, challengeId);

            ResultSet resultSet = selectContributions.executeQuery();

            List<Contribution> contributions = new LinkedList<>();
            while (resultSet.next()) {
                contributions.add(contributionFromRow(resultSet));
            }

            return contributions;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    
    private Contribution contributionFromRow(ResultSet row) throws SQLException {
        return Contribution.builder()
                .matchId(row.getInt("match_id"))
                .playerId(row.getLong("participant_id"))
                .godId(row.getInt("god_id"))
                .win(row.getBoolean("win"))
                .deaths(row.getShort("deaths"))
                .kills(row.getShort("kills"))
                .build();
    }
}
