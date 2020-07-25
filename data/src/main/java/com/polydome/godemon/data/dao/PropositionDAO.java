package com.polydome.godemon.data.dao;

import com.polydome.godemon.data.dao.common.BaseDAO;
import com.polydome.godemon.domain.entity.Proposition;
import com.polydome.godemon.domain.repository.PropositionRepository;
import com.polydome.godemon.domain.repository.exception.CRUDException;
import com.polydome.godemon.domain.repository.exception.NoSuchEntityException;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PropositionDAO extends BaseDAO implements PropositionRepository {
    public PropositionDAO(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Proposition findProposition(int challengeId, long challengerId) {
        try (final var connection = getConnection(); final var countProposedChampions =
                connection.prepareStatement("SELECT COUNT(*) as `count` FROM proposed_champion WHERE challenge_id = ? AND requester_id = ?")) {

            countProposedChampions.setInt(1, challengeId);
            countProposedChampions.setLong(2, challengerId);

            try (ResultSet championsCountResultSet = countProposedChampions.executeQuery()) {
                var builder = Proposition.builder()
                        .requesterId(challengerId)
                        .challengeId(challengeId);

                if (championsCountResultSet.next()) {
                    int count = championsCountResultSet.getInt("count");

                    if (count == 0) {
                        throw new NoSuchEntityException(Proposition.class, challengeId + ", " + challengerId);
                    }

                    int[] champions = new int[count];

                    try (final var selectProposedChampions =
                                 connection.prepareStatement("SELECT god_id FROM proposed_champion WHERE challenge_id = ? AND requester_id = ?")) {

                        selectProposedChampions.setInt(1, challengeId);
                        selectProposedChampions.setLong(2, challengerId);

                        try (final var championsResultSet = selectProposedChampions.executeQuery()) {
                            int i = 0;
                            while (championsResultSet.next()) {
                                champions[i] = (championsResultSet.getInt("god_id"));
                                i++;
                            }

                            builder.gods(champions);
                        }
                    }
                }

                return builder.build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void deleteProposition(int challengeId, long challengerId) {
        try (final var connection = getConnection(); final var deleteProposedChampions =
                connection.prepareStatement("DELETE FROM proposed_champion WHERE challenge_id = ? AND requester_id = ?")) {

            deleteProposedChampions.setInt(1, challengeId);
            deleteProposedChampions.setLong(2, challengerId);

            deleteProposedChampions.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createProposition(Proposition proposition) throws CRUDException {
        try (final var connection = getConnection(); final var insertProposedChampion =
                connection.prepareStatement("INSERT INTO proposed_champion (challenge_id, requester_id, god_id) VALUES (?, ?, ?)")) {

            insertProposedChampion.setInt(1, proposition.getChallengeId());
            insertProposedChampion.setLong(2, proposition.getRequesterId());

            for (final var champion : proposition.getGods()) {
                insertProposedChampion.setInt(3, champion);
                insertProposedChampion.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
