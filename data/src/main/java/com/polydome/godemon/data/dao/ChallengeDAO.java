package com.polydome.godemon.data.dao;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.ChallengeStage;
import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import com.polydome.godemon.domain.repository.exception.CRUDException;
import com.polydome.godemon.domain.repository.exception.NoSuchEntityException;
import com.polydome.godemon.smitedata.implementation.SmiteGameModeService;

import java.sql.*;
import java.util.*;

public class ChallengeDAO implements ChallengeRepository {
    private final PreparedStatement selectParticipantsByChallengeId;
    private final PreparedStatement insertChampionStatement;
    private final PreparedStatement insertChallengeStatement;
    private final PreparedStatement updateChallengeLastUpdateStatement;
    private final PreparedStatement insertParticipant;
    private final PreparedStatement insertOrUpdateChampion;
    private final PreparedStatement deleteChampion;
    private final PreparedStatement selectChallengeById;
    private final SmiteGameModeService gameModeService;
    private final PreparedStatement selectChampionsByChallengeId;
    private final PreparedStatement selectChallengesByParticipantId;
    private final PreparedStatement selectAllChallenges;

    public ChallengeDAO(Connection dbConnection, SmiteGameModeService gameModeService) throws SQLException {
        insertChampionStatement =
                dbConnection.prepareStatement("INSERT INTO champion (challenge_id, god_id, uses_left) VALUES (?, ?, ?)");
        insertChallengeStatement =
                dbConnection.prepareStatement("INSERT INTO challenge (last_update, gamemode_id) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
        updateChallengeLastUpdateStatement =
                dbConnection.prepareStatement("UPDATE challenge SET last_update = ? WHERE id = ?");
        selectParticipantsByChallengeId =
                dbConnection.prepareStatement("SELECT challenger.* FROM challenger INNER JOIN participant ON challenger.discord_id = participant.challenger_id WHERE challenge_id = ?");
        insertParticipant =
                dbConnection.prepareStatement("INSERT IGNORE INTO participant (challenge_id, challenger_id) VALUES (?, ?)");
        insertOrUpdateChampion =
                dbConnection.prepareStatement("INSERT INTO champion (challenge_id, god_id, uses_left) VALUES (?, ? ,?) ON DUPLICATE KEY UPDATE uses_left = ?");
        deleteChampion =
                dbConnection.prepareStatement("DELETE FROM champion WHERE god_id = ?");
        selectChallengeById =
                dbConnection.prepareStatement("SELECT id, gamemode_id, last_update FROM challenge WHERE id = ?");
        selectChampionsByChallengeId =
                dbConnection.prepareStatement("SELECT god_id, uses_left FROM challenge INNER JOIN champion ON challenge.id = champion.challenge_id WHERE challenge.id = ?");
        selectChallengesByParticipantId =
                dbConnection.prepareStatement("SELECT challenge.* FROM challenge INNER JOIN participant ON challenge.id = participant.challenge_id WHERE participant.challenger_id = ?");
        selectAllChallenges =
                dbConnection.prepareStatement("SELECT * FROM challenge");
        this.gameModeService = gameModeService;
    }

    @Override
    public Challenge createChallenge(Challenge challenge) throws CRUDException {
        try {
            final var createdChallenge = challenge.toBuilder();

            insertChallengeStatement.setTimestamp(1, Timestamp.from(challenge.getLastUpdate()));
            insertChallengeStatement.setInt(2, gameModeService.getGameModeId(challenge.getGameMode()));
            insertChallengeStatement.execute();

            try (ResultSet generatedKeys = insertChallengeStatement.getGeneratedKeys()) {
                if (generatedKeys.next())
                    createdChallenge.id(generatedKeys.getInt(1));
            }

            for (var entry : challenge.getAvailableGods().entrySet()) {
                insertChampionStatement.setInt(1, challenge.getId());
                insertChampionStatement.setInt(2, entry.getKey());
                insertChampionStatement.setInt(3, entry.getValue());
                insertChampionStatement.execute();
            }

            for (var challenger : challenge.getParticipants()) {
                insertParticipant.setInt(1, challenge.getId());
                insertParticipant.setLong(2, challenger.getId());
            }

            return createdChallenge.build();
        } catch (SQLException e) {
            throw new CRUDException("Internal query failure", e);
        }
    }

    @Override
    public Challenge findChallenge(int id) throws CRUDException {
        try {
            var challengeBuilder = Challenge.builder();

            selectChallengeById.setInt(1, id);
            ResultSet resultSet = selectChallengeById.executeQuery();

            if (resultSet.next()) {
                challengeBuilder
                        .lastUpdate(resultSet.getTimestamp("last_update").toInstant())
                        .gameMode(gameModeService.getGameModeFromId(resultSet.getInt("gamemode_id")))
                        .id(resultSet.getInt("id"));
            } else {
                throw new NoSuchEntityException(Challenge.class, String.valueOf(id));
            }
            
            challengeBuilder.availableGods(findAvailableGods(id));
            challengeBuilder.participants(findParticipants(id));

            return challengeBuilder.build();
        } catch (SQLException e) {
            throw new CRUDException("Internal query failure", e);
        }
    }

    @Override
    public List<Challenge> findAllChallenges() throws CRUDException {
        try {
            ResultSet resultSet = selectAllChallenges.executeQuery();

            List<Challenge> challenges = new LinkedList<>();
            Challenge.ChallengeBuilder builder =  Challenge.builder();
            int id;

            while (resultSet.next()) {
                id = resultSet.getInt("id");

                builder.id(id)
                        .lastUpdate(resultSet.getTimestamp("last_update").toInstant())
                        .gameMode(gameModeService.getGameModeFromId(resultSet.getInt("gamemode_id")));

                builder.availableGods(findAvailableGods(id));
                builder.participants(findParticipants(id));

                challenges.add(builder.build());
            }

            return challenges;
        } catch (SQLException e) {
            throw new CRUDException(e);
        }
    }

    private Map<Integer, Integer> findAvailableGods(int challengeId) throws SQLException {
        selectChampionsByChallengeId.setLong(1, challengeId);
        ResultSet championsRow = selectChampionsByChallengeId.executeQuery();

        Map<Integer, Integer> availableGods = new HashMap<>();

        while (championsRow.next()) {
            availableGods.put(championsRow.getInt("god_id"), championsRow.getInt("uses_left"));
        }

        return availableGods;
    }

    private List<Challenger> findParticipants(int challengeId) throws SQLException {
        List<Challenger> participants = new LinkedList<>();
        selectParticipantsByChallengeId.setInt(1, challengeId);
        ResultSet participantRow = selectParticipantsByChallengeId.executeQuery();
        while (participantRow.next()) {
            participants.add(
                    Challenger.builder()
                            .id(participantRow.getLong("discord_id"))
                            .inGameId(participantRow.getInt("hirez_id"))
                            .inGameName(participantRow.getString("hirez_name"))
                            .build()
            );
        }
        
        return participants;
    }

    @Override
    public void updateChallenge(Challenge challenge) throws CRUDException {
        try {
            selectChampionsByChallengeId.setInt(1, challenge.getId());
            ResultSet resultSet = selectChampionsByChallengeId.executeQuery();

            Set<Integer> availableGodsIds = challenge.getAvailableGods().keySet();
            Set<Integer> idsToBeRemoved = new HashSet<>();
            int id;
            while (resultSet.next()) {
                id = resultSet.getInt("god_id");
                if (!availableGodsIds.contains(id))
                    idsToBeRemoved.add(id);
            }

            for (final var godId : idsToBeRemoved) {
                deleteChampion.setInt(1, godId);
                deleteChampion.execute();
            }

            for (var entry : challenge.getAvailableGods().entrySet()) {
                insertOrUpdateChampion.setLong(1, challenge.getId());
                insertOrUpdateChampion.setInt(2, entry.getKey());
                insertOrUpdateChampion.setInt(3, entry.getValue());
                insertOrUpdateChampion.setInt(4, entry.getValue());
                insertOrUpdateChampion.execute();
            }

            for (var challenger : challenge.getParticipants()) {
                insertParticipant.setInt(1, challenge.getId());
                insertParticipant.setLong(2, challenger.getId());
                insertParticipant.execute();
            }

            updateChallengeLastUpdateStatement.setTimestamp(1, Timestamp.from(challenge.getLastUpdate()));
            updateChallengeLastUpdateStatement.setInt(2, challenge.getId());
            updateChallengeLastUpdateStatement.execute();
        } catch (SQLException e) {
            throw new CRUDException("Internal query failure", e);
        }
    }

    @Override
    public List<Challenge> findChallengesByParticipant(long participantId) throws CRUDException {
        ResultSet resultSet;
        try {
            selectChallengesByParticipantId.setLong(1, participantId);
            resultSet = selectChallengesByParticipantId.executeQuery();

            int id;
            List<Challenge> challenges = new LinkedList<>();

            while (resultSet.next()) {
                id = resultSet.getInt("id");
                challenges.add(
                    Challenge.builder()
                        .id(id)
                        .participants(findParticipants(id))
                        .gameMode(gameModeService.getGameModeFromId(resultSet.getInt("gamemode_id")))
                        .lastUpdate(resultSet.getTimestamp("last_update").toInstant())
                        .status(ChallengeStage.ONGOING)
                        .availableGods(findAvailableGods(id))
                        .build()
                );
            }

            return challenges;
        } catch (SQLException e) {
            throw new CRUDException(e);
        }
    }
}
