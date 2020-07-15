package com.polydome.godemon.data.dao;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.ChallengeStatus;
import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import com.polydome.godemon.smitedata.implementation.SmiteGameModeService;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ChallengeDAO implements ChallengeRepository {
    private final PreparedStatement selectChampionsByChallengerId;
    private final PreparedStatement selectChallengeByChallengerId;
    private final PreparedStatement selectParticipantsByChallengeId;
    private final PreparedStatement deleteAllChampionsOfChallengeStatement;
    private final PreparedStatement insertChampionStatement;
    private final PreparedStatement insertChallengeStatement;
    private final PreparedStatement updateChallengeLastUpdateStatement;
    private final PreparedStatement insertParticipant;
    private final SmiteGameModeService gameModeService;

    public ChallengeDAO(Connection dbConnection, SmiteGameModeService gameModeService) throws SQLException {
        selectChampionsByChallengerId =
                dbConnection.prepareStatement("SELECT god_id, uses_left FROM challenge INNER JOIN champion ON challenge.id = champion.challenge_id WHERE challenger_id = ?");
        selectChallengeByChallengerId =
                dbConnection.prepareStatement("SELECT * FROM challenge WHERE challenge.challenger_id = ?");
        deleteAllChampionsOfChallengeStatement =
                dbConnection.prepareStatement("DELETE FROM champion WHERE challenge_id = ?");
        insertChampionStatement =
                dbConnection.prepareStatement("INSERT INTO champion (challenge_id, god_id, uses_left) VALUES (?, ?, ?)");
        insertChallengeStatement =
                dbConnection.prepareStatement("INSERT INTO challenge (last_update, gamemode_id) VALUES (?, ?)");
        updateChallengeLastUpdateStatement =
                dbConnection.prepareStatement("UPDATE challenge SET last_update = ? WHERE challenger_id = ?");
        selectParticipantsByChallengeId =
                dbConnection.prepareStatement("SELECT id, hirez_id, hirez_name FROM challenger INNER JOIN challenge ON challenge.id = ?");
        insertParticipant =
                dbConnection.prepareStatement("INSERT INTO participant (challenge_id, challenger_id) VALUES (?, ?)");
        this.gameModeService = gameModeService;
    }

    @Override
    public Challenge findChallengeByChallengerId(long id) {
        try {
            var challengeBuilder = Challenge.builder();

            selectChallengeByChallengerId.setLong(1, id);
            ResultSet challengeRow = selectChallengeByChallengerId.executeQuery();
            int gameModeId = challengeRow.getInt("gamemode_id");
            int challengeId = challengeRow.getInt("challenge_id");
            challengeBuilder
                    .id(challengeId)
                    .gameMode(gameModeService.getGameModeFromId(gameModeId))
                    .lastUpdate(challengeRow.getTimestamp("last_update").toInstant())
                    .status(ChallengeStatus.ONGOING);

            selectChampionsByChallengerId.setLong(1, id);
            ResultSet championsRow = selectChampionsByChallengerId.executeQuery();
            Map<Integer, Integer> availableGods = new HashMap<>();
            while (championsRow.next()) {
                availableGods.put(championsRow.getInt("god_id"), championsRow.getInt("uses_left"));
            }
            challengeBuilder.availableGods(availableGods);

            List<Challenger> participants = new LinkedList<>();
            selectParticipantsByChallengeId.setInt(1, challengeId);
            ResultSet participantRow = selectParticipantsByChallengeId.executeQuery();
            while (participantRow.next()) {
                participants.add(
                    Challenger.builder()
                            .id(participantRow.getInt("id"))
                            .inGameName(participantRow.getString("hirez_id"))
                            .inGameId(participantRow.getInt("hirez_name"))
                        .build()
                );
            }
            challengeBuilder.participants(participants);

            return challengeBuilder.build();
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }

        return null;
    }

    @Override
    public void createChallenge(Challenge challenge) {
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
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void updateChallenge(Challenge challenge) {
        try {
            selectChallengeByChallengerId.setLong(1, challengerId);
            ResultSet row = selectChallengeByChallengerId.executeQuery();

            row.next();
            int challengeId = row.getInt("id");

            deleteAllChampionsOfChallengeStatement.setLong(1, challengerId);
            deleteAllChampionsOfChallengeStatement.execute();

            for (var entry : challenge.getAvailableGods().entrySet()) {
                    insertChampionStatement.setLong(1, challengeId);
                    insertChampionStatement.setInt(2, entry.getKey());
                    insertChampionStatement.setInt(3, entry.getValue());
                    insertChampionStatement.execute();
            }

            updateChallengeLastUpdateStatement.setTimestamp(1, Timestamp.from(challenge.getLastUpdate()));
            updateChallengeLastUpdateStatement.setInt(2, challengeId);
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
    }
}
