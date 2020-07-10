package com.polydome.godemon.data.dao;

import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.repository.ChallengerRepository;

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
                dbConnection.prepareStatement("INSERT INTO challenger (discord_id, hirez_name) VALUES (?, ?)");
    }

    @Override
    public Challenger findByDiscordId(long id) {
        try {
            findByDiscordIdStatement.setLong(1, id);
            ResultSet row = findByDiscordIdStatement.executeQuery();

            if (row.next()) {
                return new Challenger(row.getLong("discord_id"), row.getString("hirez_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void insert(long discordId, String inGameName) {
        try {
            insertStatement.setLong(1, discordId);
            insertStatement.setString(2, inGameName);
            insertStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
