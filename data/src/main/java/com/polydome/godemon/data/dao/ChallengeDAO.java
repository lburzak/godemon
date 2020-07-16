package com.polydome.godemon.data.dao;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import com.polydome.godemon.domain.repository.exception.CRUDException;
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

    public ChallengeDAO(Connection dbConnection, SmiteGameModeService gameModeService) throws SQLException {
        insertChampionStatement =
                dbConnection.prepareStatement("INSERT INTO champion (challenge_id, god_id, uses_left) VALUES (?, ?, ?)");
        insertChallengeStatement =
                dbConnection.prepareStatement("INSERT INTO challenge (last_update, gamemode_id) VALUES (?, ?)");
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
        this.gameModeService = gameModeService;
    }

    @Override
    public void createChallenge(Challenge challenge) throws CRUDException {
        try {
            insertChallengeStatement.setTimestamp(1, Timestamp.from(challenge.getLastUpdate()));
            insertChallengeStatement.setInt(2, gameModeService.getGameModeId(challenge.getGameMode()));
            insertChallengeStatement.execute();

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
                        .id(resultSet.getInt(id));
            }

            selectChampionsByChallengeId.setLong(1, id);
            ResultSet championsRow = selectChampionsByChallengeId.executeQuery();
            Map<Integer, Integer> availableGods = new HashMap<>();
            while (championsRow.next()) {
                availableGods.put(championsRow.getInt("god_id"), championsRow.getInt("uses_left"));
            }
            challengeBuilder.availableGods(availableGods);

            List<Challenger> participants = new LinkedList<>();
            selectParticipantsByChallengeId.setInt(1, id);
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
            challengeBuilder.participants(participants);

            return challengeBuilder.build();
        } catch (SQLException e) {
            throw new CRUDException("Internal query failure", e);
        }
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
}
