package com.polydome.godemon.data;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.Proposition;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import com.polydome.godemon.domain.repository.PropositionRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChallengeDAO implements ChallengeRepository, PropositionRepository {
    private final PreparedStatement selectChampionsByChallengerId;
    private final PreparedStatement selectChallengeByChallengerId;
    private final PreparedStatement deleteAllChampionsOfChallengeStatement;
    private final PreparedStatement insertChampionStatement;
    private final PreparedStatement findPropositionMessageIdByChallengerIdStatement;
    private final PreparedStatement findGodsIdsByChallengerIdStatement;
    private final PreparedStatement updatePropositionMessageIdStatement;
    private final PreparedStatement insertChallengeStatement;
    private final PreparedStatement clearPropositionStatement;

    public ChallengeDAO(Connection dbConnection) throws SQLException {
        selectChampionsByChallengerId =
                dbConnection.prepareStatement("SELECT god_id, uses_left FROM challenge INNER JOIN champion ON challenge.id = champion.challenge_id WHERE challenger_id = ?");
        selectChallengeByChallengerId =
                dbConnection.prepareStatement("SELECT * FROM challenge WHERE challenge.challenger_id = ?");
        deleteAllChampionsOfChallengeStatement =
                dbConnection.prepareStatement("DELETE FROM champion WHERE challenge_id = (SELECT id FROM challenge WHERE challenger_id = ?)");
        insertChampionStatement =
                dbConnection.prepareStatement("INSERT INTO champion (challenge_id, god_id, uses_left) VALUES (?, ?, ?)");
        findPropositionMessageIdByChallengerIdStatement =
                dbConnection.prepareStatement("SELECT id, proposition_message_id FROM challenge WHERE challenger_id = ?");
        findGodsIdsByChallengerIdStatement =
                dbConnection.prepareStatement("SELECT god_id FROM champion WHERE challenge_id = ?");
        updatePropositionMessageIdStatement =
                dbConnection.prepareStatement("UPDATE challenge SET proposition_message_id = ? WHERE challenger_id = ?");
        insertChallengeStatement =
                dbConnection.prepareStatement("INSERT INTO challenge (challenger_id) VALUES (?)");
        clearPropositionStatement =
                dbConnection.prepareStatement("UPDATE challenge SET proposition_message_id = NULL WHERE challenger_id = ?");
    }

    @Override
    public Proposition findPropositionByChallengerId(long id) {
        try {
            findPropositionMessageIdByChallengerIdStatement.setLong(1, id);
            ResultSet row = findPropositionMessageIdByChallengerIdStatement.executeQuery();

            if (row.next()) {
                int challengeId = row.getInt("id");
                long messageId = row.getLong("proposition_message_id");

                if (messageId == 0) {
                    return null;
                }

                findGodsIdsByChallengerIdStatement.setInt(1, challengeId);
                row = findGodsIdsByChallengerIdStatement.executeQuery();

                List<Integer> godsIds = new ArrayList<>();

                while (row.next()) {
                    godsIds.add(row.getInt("god_id"));
                }

                int[] godsIdsArray = new int[godsIds.size()];

                var iter = godsIds.iterator();
                int i = 0;
                while (iter.hasNext()) {
                    godsIdsArray[i++] = iter.next();
                }

                return new Proposition(id, godsIdsArray, 0, messageId);
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }

        return null;
    }

    @Override
    public Challenge findChallengeByChallengerId(long id) {
        try {
            Map<Integer, Integer> availableGods = new HashMap<>();

            selectChampionsByChallengerId.setLong(1, id);
            ResultSet row = selectChampionsByChallengerId.executeQuery();

            while (row.next()) {
                availableGods.put(row.getInt("god_id"), row.getInt("uses_left"));
            }

            return new Challenge(availableGods);
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }

        return null;
    }

    @Override
    public void createProposition(long challengerId, int[] gods, int rerolls, long messageId) {
        try {
            selectChallengeByChallengerId.setLong(1, challengerId);
            ResultSet row = selectChallengeByChallengerId.executeQuery();

            row.next();
            int challengeId = row.getInt("id");

            for (int god : gods) {
                insertChampionStatement.setLong(1, challengeId);
                insertChampionStatement.setInt(2, god);
                insertChampionStatement.setInt(3, 0);
                insertChampionStatement.execute();
            }

            updatePropositionMessageIdStatement.setLong(1, messageId);
            updatePropositionMessageIdStatement.setLong(2, challengerId);
            updatePropositionMessageIdStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Replaces all champions assigned to a challenger with those listed in {@code availableGods}
     * @param challengerId Id of challenger
     * @param availableGods Map of champion id to uses left count
     */
    @Override
    public void createChallenge(long challengerId, Map<Integer, Integer> availableGods) {
        try {
            insertChallengeStatement.setLong(1, challengerId);
            insertChallengeStatement.execute();

            deleteAllChampionsOfChallengeStatement.setLong(1, challengerId);
            deleteAllChampionsOfChallengeStatement.execute();

            for (var entry : availableGods.entrySet()) {
                insertChampionStatement.setLong(1, challengerId);
                insertChampionStatement.setInt(2, entry.getKey());
                insertChampionStatement.setInt(3, entry.getValue());
                insertChampionStatement.execute();
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void updateChallenge(long challengerId, Challenge newChallenge) {
        try {
            selectChallengeByChallengerId.setLong(1, challengerId);
            ResultSet row = selectChallengeByChallengerId.executeQuery();

            row.next();
            int challengeId = row.getInt("id");

            deleteAllChampionsOfChallengeStatement.setLong(1, challengerId);
            deleteAllChampionsOfChallengeStatement.execute();

            for (var entry : newChallenge.getAvailableGods().entrySet()) {
                    insertChampionStatement.setLong(1, challengeId);
                    insertChampionStatement.setInt(2, entry.getKey());
                    insertChampionStatement.setInt(3, entry.getValue());
                    insertChampionStatement.execute();
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void deleteProposition(long challengerId) {
        try {
            clearPropositionStatement.setLong(1, challengerId);
            clearPropositionStatement.execute();
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
    }
}
