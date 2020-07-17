package com.polydome.godemon.data.dao;

import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.repository.ChallengerRepository;
import com.polydome.godemon.domain.repository.exception.CRUDException;
import com.polydome.godemon.domain.repository.exception.NoSuchEntityException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChallengerDAO implements ChallengerRepository {
    private final PreparedStatement findByDiscordIdStatement;
    private final PreparedStatement insertStatement;

    public ChallengerDAO(Connection dbConnection) throws SQLException {
        this.findByDiscordIdStatement =
                dbConnection.prepareStatement("SELECT * FROM challenger WHERE discord_id = ? LIMIT 1");
        this.insertStatement =
                dbConnection.prepareStatement("INSERT INTO challenger (discord_id, hirez_name, hirez_id) VALUES (?, ?, ?)");
    }

    @Override
    public Challenger findChallengerById(long id) {
        try {
            findByDiscordIdStatement.setLong(1, id);
            ResultSet row = findByDiscordIdStatement.executeQuery();

            if (row.next()) {
                return Challenger.builder()
                        .id(row.getLong("discord_id"))
                        .inGameName(row.getString("hirez_name"))
                        .inGameId(row.getInt("hirez_id"))
                        .build();
            } else {
                throw new NoSuchEntityException(Challenger.class, String.valueOf(id));
            }
        } catch (SQLException e) {
            throw new CRUDException(e);
        }
    }

    @Override
    public void createChallenger(long discordId, String inGameName, int inGameId) {
        try {
            insertStatement.setLong(1, discordId);
            insertStatement.setString(2, inGameName);
            insertStatement.setInt(3, inGameId);
            insertStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
