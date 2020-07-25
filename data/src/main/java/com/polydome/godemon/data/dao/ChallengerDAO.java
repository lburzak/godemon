package com.polydome.godemon.data.dao;

import com.polydome.godemon.data.dao.common.BaseDAO;
import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.repository.ChallengerRepository;
import com.polydome.godemon.domain.repository.exception.CRUDException;
import com.polydome.godemon.domain.repository.exception.NoSuchEntityException;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChallengerDAO extends BaseDAO implements ChallengerRepository {
    public ChallengerDAO(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Challenger findChallengerById(long id) throws NoSuchEntityException {
        try (final var connection = getConnection(); final var findByDiscordId =
                connection.prepareStatement("SELECT * FROM challenger WHERE discord_id = ? LIMIT 1")) {

            findByDiscordId.setLong(1, id);

            try (ResultSet row = findByDiscordId.executeQuery()) {
                if (row.next()) {
                    return challengerFromRow(row);
                } else {
                    throw new NoSuchEntityException(Challenger.class, String.valueOf(id));
                }
            }
        } catch (SQLException e) {
            throw new CRUDException(e);
        }
    }

    @Override
    public boolean existsChallenger(long id) throws CRUDException {
        try (final var connection = getConnection(); final var findByDiscordId =
                connection.prepareStatement("SELECT * FROM challenger WHERE discord_id = ? LIMIT 1")) {

            findByDiscordId.setLong(1, id);

            try (ResultSet resultSet = findByDiscordId.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new CRUDException(e);
        }
    }

    @Override
    public void createChallenger(long discordId, String inGameName, int inGameId) {
        try (final var connection = getConnection(); final var insertStatement =
                connection.prepareStatement("INSERT INTO challenger (discord_id, hirez_name, hirez_id) VALUES (?, ?, ?)")) {

            insertStatement.setLong(1, discordId);
            insertStatement.setString(2, inGameName);
            insertStatement.setInt(3, inGameId);

            insertStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Challenger challengerFromRow(ResultSet row) throws SQLException {
        return Challenger.builder()
                .id(row.getLong("discord_id"))
                .inGameId(row.getInt("hirez_id"))
                .inGameName(row.getString("hirez_name"))
                .build();
    }
}
