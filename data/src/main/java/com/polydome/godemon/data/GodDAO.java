package com.polydome.godemon.data;

import com.polydome.godemon.smitedata.entity.God;
import com.polydome.godemon.smitedata.repository.GodsRepository;
import io.reactivex.Completable;
import io.reactivex.Maybe;

import java.sql.*;

public class GodDAO implements GodsRepository {
    private final Connection connection;
    private final PreparedStatement findByNameStatement;
    private final PreparedStatement insertIfNotExistsStatement;

    private final String LABEL_ID = "id";
    private final String LABEL_NAME = "name";
    private final String LABEL_LOCALIZED_NAME = "name_en";

    public GodDAO(Connection connection) throws SQLException {
        this.connection = connection;
        findByNameStatement =
                connection.prepareStatement("SELECT * FROM god WHERE name_en LIKE ? LIMIT 1");
        insertIfNotExistsStatement =
                connection.prepareStatement("INSERT IGNORE INTO god(name, name_en) VALUES (?, ?)");
    }

    private God serializeResult(ResultSet result) throws SQLException {
        return new God(
                result.getInt(LABEL_ID),
                result.getString(LABEL_NAME),
                result.getString(LABEL_LOCALIZED_NAME)
        );
    }

    @Override
    public Maybe<God> findByName(String name) {
        return Maybe.create(emitter -> {
            findByNameStatement.setString(1, name);
            ResultSet result = findByNameStatement.executeQuery();

            if (result.next())
                emitter.onSuccess(serializeResult(result));
            else
                emitter.onComplete();
        });
    }

    @Override
    public Completable insertIfNotExists(God god) {
        return Completable.create(emitter -> {
            insertIfNotExistsStatement.setString(1, god.name);
            insertIfNotExistsStatement.setString(2, god.displayName);

            insertIfNotExistsStatement.execute();
            emitter.onComplete();
        });
    }
}
