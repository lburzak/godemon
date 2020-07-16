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
    private final PreparedStatement selectProposition;
    private final PreparedStatement selectProposedChampions;
    private final PreparedStatement countProposedChampions;
    private final PreparedStatement insertProposition;
    private final PreparedStatement insertProposedChampion;
    private final PreparedStatement deleteProposedChampions;
    private final PreparedStatement deleteProposition;

    public PropositionDAO(Connection dbConnection) throws SQLException {
        selectProposition =
                dbConnection.prepareStatement("SELECT requester_id, challenge_id FROM proposition_message WHERE id = ?");
        countProposedChampions =
                dbConnection.prepareStatement("SELECT COUNT(*) as `count` FROM proposed_champion WHERE proposition_message_id = ?");
        selectProposedChampions =
                dbConnection.prepareStatement("SELECT god_id FROM proposed_champion WHERE proposition_message_id = ?");
        insertProposition =
                dbConnection.prepareStatement("INSERT INTO proposition_message (id, challenge_id, requester_id) VALUES (?, ?, ?)");
        insertProposedChampion =
                dbConnection.prepareStatement("INSERT INTO proposed_champion (proposition_message_id, god_id) VALUES (?, ?)");
        deleteProposedChampions =
                dbConnection.prepareStatement("DELETE FROM proposed_champion WHERE proposition_message_id = ?");
        deleteProposition =
                dbConnection.prepareStatement("DELETE FROM proposition_message WHERE id = ?");
    }

    @Override
    public Proposition findProposition(long messageId) {
        try {
            selectProposition.setLong(1, messageId);
            ResultSet resultSet = selectProposition.executeQuery();

            if (resultSet.next()) {
                var builder = Proposition.builder();
                builder.messageId(messageId);
                builder.challengeId(resultSet.getInt("challenge_id"));
                builder.requesterId(resultSet.getLong("requester_id"));

                countProposedChampions.setLong(1, messageId);
                resultSet = countProposedChampions.executeQuery();

                if (!resultSet.next())
                    return builder.build();

                int count = resultSet.getInt("count");
                int[] champions = new int[count];

                selectProposedChampions.setLong(1, messageId);
                resultSet = selectProposedChampions.executeQuery();
                int i = 0;
                while (resultSet.next()) {
                    champions[i] = (resultSet.getInt("god_id"));
                    i++;
                }

                builder.gods(champions);
                return builder.build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void createProposition(Proposition proposition) throws CRUDException {
        try {
            insertProposition.setLong(1, proposition.getMessageId());
            insertProposition.setInt(2, proposition.getChallengeId());
            insertProposition.setLong(3, proposition.getRequesterId());

            try {
                insertProposition.execute();
            } catch (SQLException e) {
                if (e.getMessage().startsWith("Duplicate")) {
                    throw new DuplicateEntryException(Proposition.class.getName());
                }
            }

            for (final var champion : proposition.getGods()) {
                insertProposedChampion.setLong(1, proposition.getMessageId());
                insertProposedChampion.setInt(2, champion);
                insertProposedChampion.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteProposition(long messageId) {
        try {
            deleteProposedChampions.setLong(1, messageId);
            deleteProposedChampions.execute();

            deleteProposition.setLong(1, messageId);
            deleteProposition.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
