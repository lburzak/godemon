package com.polydome.godemon.data.dao;

import com.polydome.godemon.smitedata.entity.Emote;
import com.polydome.godemon.smitedata.repository.EmoteRepository;
import io.reactivex.Completable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EmoteDAO implements EmoteRepository {
    private final PreparedStatement insertStatement;

    public EmoteDAO(Connection connection) throws SQLException {
        insertStatement = connection.prepareStatement(
                "INSERT IGNORE INTO emote(god_id, display_id, hosted_id, host_id) VALUES (?, ?, ?, ?)"
        );
    }

    @Override
    public Completable insert(Emote emote) {
        return Completable.create(emitter -> {
            insertStatement.setInt(1, emote.godId);
            insertStatement.setString(2, emote.displayId);
            insertStatement.setLong(3, emote.hostedId);
            insertStatement.setInt(4, emote.hostId);

            insertStatement.execute();
            emitter.onComplete();
        });
    }
}
