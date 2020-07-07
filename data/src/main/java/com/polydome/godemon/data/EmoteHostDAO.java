package com.polydome.godemon.data;

import com.polydome.godemon.smitedata.entity.EmoteHost;
import com.polydome.godemon.smitedata.repository.EmoteHostRepository;
import io.reactivex.Observable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmoteHostDAO implements EmoteHostRepository {
    private final Connection dbConnection;
    private final PreparedStatement findAllStatement;

    public EmoteHostDAO(Connection dbConnection) throws SQLException {
        this.dbConnection = dbConnection;

        findAllStatement = dbConnection.prepareStatement(
                "SELECT * FROM emote_host"
        );
    }

    private EmoteHost serializeResultRow(ResultSet resultSet) throws SQLException {
        return new EmoteHost(
                resultSet.getInt("id"),
                resultSet.getLong("guild_id")
        );
    }

    @Override
    public Observable<EmoteHost> findAll() {
        return Observable.create(emitter -> {
            ResultSet result = findAllStatement.executeQuery();

            while (result.next()) {
                emitter.onNext(serializeResultRow(result));
            }

            emitter.onComplete();
        });
    }
}
