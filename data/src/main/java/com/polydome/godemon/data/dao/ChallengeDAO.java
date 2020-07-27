package com.polydome.godemon.data.dao;

import com.polydome.godemon.data.dao.common.BaseDAO;
import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import com.polydome.godemon.domain.repository.exception.CRUDException;
import com.polydome.godemon.domain.repository.exception.NoSuchEntityException;
import com.polydome.godemon.smitedata.implementation.SmiteGameModeService;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.*;

public class ChallengeDAO extends BaseDAO implements ChallengeRepository {
    private final SmiteGameModeService gameModeService;

    public ChallengeDAO(DataSource dataSource, SmiteGameModeService gameModeService) {
        super(dataSource);
        this.gameModeService = gameModeService;
    }

    @Override
    public Challenge createChallenge(Challenge challenge) throws CRUDException {
        try {
            int id = insertChallenge(challenge);
            insertChampions(challenge.getId(), challenge.getAvailableGods());
            insertNotExistingParticipants(challenge.getId(), challenge.getParticipants());

            return challenge.toBuilder()
                    .id(id)
                    .build();
        } catch (SQLException e) {
            throw new CRUDException("Internal query failure", e);
        }
    }

    @Override
    public Challenge findChallenge(int id) throws CRUDException {
        try (final var connection = getConnection(); final var selectChallengeById =
                connection.prepareStatement("SELECT * FROM challenge WHERE id = ?")) {
            selectChallengeById.setInt(1, id);

            try (ResultSet resultSet = selectChallengeById.executeQuery()) {
                if (resultSet.next()) {
                    return challengeFromRow(resultSet, Challenge.builder());
                } else {
                    throw new NoSuchEntityException(Challenge.class, String.valueOf(id));
                }
            }
        } catch (SQLException e) {
            throw new CRUDException("Internal query failure", e);
        }
    }

    @Override
    public List<Challenge> findAllChallenges() throws CRUDException {
        try (final var connection = getConnection()) {
            try (final var selectAllChallenges =
                         connection.prepareStatement("SELECT * FROM challenge")) {
                ResultSet resultSet = selectAllChallenges.executeQuery();

                List<Challenge> challenges = new LinkedList<>();
                Challenge.ChallengeBuilder builder =  Challenge.builder();

                while (resultSet.next()) {
                    challenges.add(challengeFromRow(resultSet, builder));
                }

                return challenges;
            }
        } catch (SQLException e) {
            throw new CRUDException(e);
        }
    }

    @Override
    public void updateChallenge(Challenge challenge) throws CRUDException {
        try {
            updateChampions(challenge.getId(), challenge.getAvailableGods());
            insertNotExistingParticipants(challenge.getId(), challenge.getParticipants());
            touchChallenge(challenge.getId(), challenge.getLastUpdate());
        } catch (SQLException e) {
            throw new CRUDException("Internal query failure", e);
        }
    }

    @Override
    public List<Challenge> findChallengesByParticipant(long participantId) throws CRUDException {
        try (final var connection = getConnection()) {
            try (final var selectChallengesByParticipantId =
                         connection.prepareStatement("SELECT challenge.* FROM challenge INNER JOIN participant ON challenge.id = participant.challenge_id WHERE participant.challenger_id = ?")) {
                selectChallengesByParticipantId.setLong(1, participantId);

                try (ResultSet resultSet = selectChallengesByParticipantId.executeQuery()) {
                    List<Challenge> challenges = new LinkedList<>();
                    Challenge.ChallengeBuilder builder = Challenge.builder();

                    while (resultSet.next()) {
                        challenges.add(challengeFromRow(resultSet, builder));
                    }

                    return challenges;
                }

            }
        } catch (SQLException e) {
            throw new CRUDException(e);
        }
    }

    /**
     * @return Inserted challenge id
     */
    private int insertChallenge(Challenge challenge) throws SQLException {
        try (final var connection = getConnection(); final var insertChallenge =
                connection.prepareStatement("INSERT INTO challenge (last_update, created_at, gamemode_id) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)
        ) {
            insertChallenge.setTimestamp(1, Timestamp.from(challenge.getLastUpdate()));
            insertChallenge.setTimestamp(2, Timestamp.from(challenge.getCreatedAt()));
            insertChallenge.setInt(3, gameModeService.getGameModeId(challenge.getGameMode()));
            insertChallenge.execute();

            try (ResultSet generatedKeys = insertChallenge.getGeneratedKeys()) {
                if (generatedKeys.next())
                    return generatedKeys.getInt(1);
                else
                    return 0;
            }
        }
    }

    private void touchChallenge(int challengeId, Instant newDate) throws SQLException {
        try (final var connection = getConnection(); final var updateChallengeLastUpdateStatement =
                connection.prepareStatement("UPDATE challenge SET last_update = ? WHERE id = ?")) {

            updateChallengeLastUpdateStatement.setTimestamp(1, Timestamp.from(newDate));
            updateChallengeLastUpdateStatement.setInt(2, challengeId);
            updateChallengeLastUpdateStatement.execute();
        }
    }

    private Map<Integer, Integer> findChampions(int challengeId) throws SQLException {
        try (final var connection = getConnection(); final var selectChampionsByChallengeId =
                connection.prepareStatement("SELECT god_id, uses_left FROM challenge INNER JOIN champion ON challenge.id = champion.challenge_id WHERE challenge.id = ?")) {
            selectChampionsByChallengeId.setLong(1, challengeId);

            try (ResultSet championsRow = selectChampionsByChallengeId.executeQuery()) {
                Map<Integer, Integer> availableGods = new HashMap<>();

                while (championsRow.next()) {
                    availableGods.put(championsRow.getInt("god_id"), championsRow.getInt("uses_left"));
                }

                return availableGods;
            }
        }
    }

    private void insertChampions(int challengeId, Map<Integer, Integer> championsToUsesLeft) throws SQLException {
        try (final var connection = getConnection(); final var insertChampion =
                connection.prepareStatement("INSERT INTO champion (challenge_id, god_id, uses_left) VALUES (?, ?, ?)")) {
            for (var entry : championsToUsesLeft.entrySet()) {
                insertChampion.setInt(1, challengeId);
                insertChampion.setInt(2, entry.getKey());
                insertChampion.setInt(3, entry.getValue());
                insertChampion.execute();
            }
        }
    }

    private void updateChampions(int challengeId, Map<Integer, Integer> championIdToUsesLeft) throws SQLException {
        try (final var connection = getConnection()) {
            try (final var selectAvailableGodsIds =
                    connection.prepareStatement("SELECT god_id FROM god INNER JOIN champion ON god.id = champion.god_id WHERE challenge_id = ?")) {

                selectAvailableGodsIds.setInt(1, challengeId);

                try (ResultSet resultSet = selectAvailableGodsIds.executeQuery()) {
                    if (resultSet.next()) {
                        Set<Integer> availableGodsIds = championIdToUsesLeft.keySet();
                        Set<Integer> idsToBeRemoved = new HashSet<>();
                        int id;

                        do {
                            id = resultSet.getInt("god_id");
                            if (!availableGodsIds.contains(id))
                                idsToBeRemoved.add(id);
                        } while (resultSet.next());

                        try (final var deleteChampion =
                                     connection.prepareStatement("DELETE FROM champion WHERE god_id = ?")) {

                            for (final var godId : idsToBeRemoved) {
                                deleteChampion.setInt(1, godId);
                                deleteChampion.execute();
                            }
                        }
                    }
                }
            }

            try (final var insertOrUpdateChampion =
                         connection.prepareStatement("INSERT INTO champion (challenge_id, god_id, uses_left) VALUES (?, ? ,?) ON DUPLICATE KEY UPDATE uses_left = ?")) {
                for (var entry : championIdToUsesLeft.entrySet()) {
                    insertOrUpdateChampion.setLong(1, challengeId);
                    insertOrUpdateChampion.setInt(2, entry.getKey());
                    insertOrUpdateChampion.setInt(3, entry.getValue());
                    insertOrUpdateChampion.setInt(4, entry.getValue());
                    insertOrUpdateChampion.execute();
                }
            }
        }
    }

    private List<Challenger> findParticipants(int challengeId) throws SQLException {
        try (final var connection = getConnection(); final var selectParticipantsByChallengeId =
                connection.prepareStatement("SELECT challenger.* FROM challenger INNER JOIN participant ON challenger.discord_id = participant.challenger_id WHERE challenge_id = ?")) {
            selectParticipantsByChallengeId.setInt(1, challengeId);

            try (ResultSet participantRow = selectParticipantsByChallengeId.executeQuery()) {
                List<Challenger> participants = new LinkedList<>();
                while (participantRow.next()) {
                    participants.add(challengerFromRow(participantRow));
                }

                return participants;
            }
        }
    }

    private void insertNotExistingParticipants(int challengeId, List<Challenger> participants) throws SQLException {
        try (final var connection = getConnection(); final var insertParticipant =
                connection.prepareStatement("INSERT IGNORE INTO participant (challenge_id, challenger_id) VALUES (?, ?)")) {
            for (final var participant : participants) {
                insertParticipant.setInt(1, challengeId);
                insertParticipant.setLong(2, participant.getId());

                insertParticipant.execute();
            }
        }
    }

    private Challenge challengeFromRow(ResultSet resultSet, Challenge.ChallengeBuilder builder) throws SQLException {
        final int id = resultSet.getInt("id");

        builder
                .id(id)
                .lastUpdate(resultSet.getTimestamp("last_update").toInstant())
                .createdAt(resultSet.getTimestamp("created_at").toInstant())
                .gameMode(gameModeService.getGameModeFromId(resultSet.getInt("gamemode_id")))
                .availableGods(findChampions(id))
                .participants(findParticipants(id));

        return builder.build();
    }

    private Challenger challengerFromRow(ResultSet row) throws SQLException {
        return Challenger.builder()
                .id(row.getLong("discord_id"))
                .inGameId(row.getInt("hirez_id"))
                .inGameName(row.getString("hirez_name"))
                .build();
    }
}
