package com.polydome.godemon.data.dao;

import com.polydome.godemon.domain.entity.Proposition;
import com.polydome.godemon.domain.repository.PropositionRepository;
import com.polydome.godemon.domain.repository.exception.CRUDException;
import com.polydome.godemon.domain.repository.exception.DuplicateEntryException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PropositionDAO implements PropositionRepository {
    private final PreparedStatement selectProposedChampions;
    private final PreparedStatement countProposedChampions;
    private final PreparedStatement insertProposedChampion;
    private final PreparedStatement deleteProposedChampions;

    public PropositionDAO(Connection dbConnection) throws SQLException {
        countProposedChampions =
                dbConnection.prepareStatement("SELECT COUNT(*) as `count` FROM proposed_champion WHERE challenge_id = ? AND requester_id = ?");
        selectProposedChampions =
                dbConnection.prepareStatement("SELECT god_id FROM proposed_champion WHERE challenge_id = ? AND requester_id = ?");
        insertProposedChampion =
                dbConnection.prepareStatement("INSERT INTO proposed_champion (challenge_id, requester_id, god_id) VALUES (?, ?, ?)");
        deleteProposedChampions =
                dbConnection.prepareStatement("DELETE FROM proposed_champion WHERE challenge_id = ? AND requester_id = ?");
    }

    @Override
    public Proposition findProposition(int challengeId, long challengerId) {
        try {            
            countProposedChampions.setInt(1, challengeId);
            countProposedChampions.setLong(2, challengerId);
            ResultSet resultSet = countProposedChampions.executeQuery();

            int count = resultSet.getInt("count");
            int[] champions = new int[count];

            selectProposedChampions.setInt(1, challengeId);
            selectProposedChampions.setLong(2, challengerId);
            resultSet = selectProposedChampions.executeQuery();
            
            int i = 0;
            while (resultSet.next()) {
                champions[i] = (resultSet.getInt("god_id"));
                i++;
            }
            
            return Proposition.builder()
                    .requesterId(challengerId)
                    .challengeId(challengeId)
                    .gods(champions)
                    .build();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void deleteProposition(int challengeId, long challengerId) {
        try {
            deleteProposedChampions.setInt(1, challengeId);
            deleteProposedChampions.setLong(2, challengerId);

            deleteProposedChampions.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createProposition(Proposition proposition) throws CRUDException {
        try {
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
