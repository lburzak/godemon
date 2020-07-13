package com.polydome.godemon.data.dao;

import com.polydome.godemon.smitedata.entity.God;
import com.polydome.godemon.smitedata.entity.SmiteChampion;
import com.polydome.godemon.smitedata.repository.GodsRepository;
import com.polydome.godemon.smitedata.repository.SmiteChampionRepository;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

import java.sql.*;

public class GodDAO implements GodsRepository, SmiteChampionRepository {
    private final Connection connection;
    private final PreparedStatement findByNameStatement;
    private final PreparedStatement insertIfNotExistsStatement;
    private final PreparedStatement countAllStatement;
    private final PreparedStatement findByIdWithEmoteStatement;
    private final PreparedStatement findByEmoteStatement;
    private final PreparedStatement getRandomIdsStatement;

    private final String LABEL_ID = "id";
    private final String LABEL_NAME = "name";
    private final String LABEL_LOCALIZED_NAME = "name_en";
    private final String LABEL_COUNT = "count";
    private final String LABEL_EMOTE_DISPLAY_ID = "display_id";

    public GodDAO(Connection connection) throws SQLException {
        this.connection = connection;
        findByNameStatement =
                connection.prepareStatement("SELECT * FROM god WHERE name LIKE ? LIMIT 1");
        insertIfNotExistsStatement =
                connection.prepareStatement("INSERT IGNORE INTO god(id, name, name_en) VALUES (?, ?, ?)");
        countAllStatement =
                connection.prepareStatement("SELECT COUNT(*) as count FROM god");
        findByIdWithEmoteStatement =
                connection.prepareStatement("SELECT god.id, god.name, god.name_en, emote.display_id FROM god INNER JOIN emote ON god.id = emote.god_id WHERE god.id = ?");
        findByEmoteStatement =
                connection.prepareStatement("SELECT god.id, god.name, god.name_en, emote.display_id FROM god INNER JOIN emote ON god.id = emote.god_id WHERE emote.display_id LIKE ?");
        getRandomIdsStatement =
                connection.prepareStatement("SELECT id FROM god ORDER BY rand() limit ?");
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
            insertIfNotExistsStatement.setInt(1, god.id);
            insertIfNotExistsStatement.setString(2, god.name);
            insertIfNotExistsStatement.setString(3, god.displayName);

            insertIfNotExistsStatement.execute();
            emitter.onComplete();
        });
    }

    @Override
    public Single<Integer> countAll() {
        return Single.create(emitter -> {
           ResultSet result = countAllStatement.executeQuery();
           emitter.onSuccess(result.getInt(LABEL_COUNT));
        });
    }

    @Override
    public Maybe<SmiteChampion> findById(int id) {
        return Maybe.create(emitter -> {
            findByIdWithEmoteStatement.setInt(1, id);
            ResultSet result = findByIdWithEmoteStatement.executeQuery();
            if (result.next()) {
                SmiteChampion champion = new SmiteChampion(
                        result.getInt(LABEL_ID),
                        result.getString(LABEL_NAME),
                        result.getString(LABEL_LOCALIZED_NAME),
                        result.getString(LABEL_EMOTE_DISPLAY_ID)
                );

                emitter.onSuccess(champion);
            } else {
                emitter.onComplete();
            }
        });
    }

    @Override
    public Maybe<SmiteChampion> findByEmote(String emoteId) {
        return Maybe.create(emitter -> {
            findByEmoteStatement.setString(1, emoteId);
            ResultSet result = findByEmoteStatement.executeQuery();
            if (result.next()) {
                SmiteChampion champion = new SmiteChampion(
                        result.getInt(LABEL_ID),
                        result.getString(LABEL_NAME),
                        result.getString(LABEL_LOCALIZED_NAME),
                        result.getString(LABEL_EMOTE_DISPLAY_ID)
                );

                emitter.onSuccess(champion);
            } else {
                emitter.onComplete();
            }
        });
    }

    @Override
    public Maybe<int[]> getRandomIds(int count) {
        return Maybe.create(emitter -> {
            getRandomIdsStatement.setInt(1, count);
            ResultSet result = getRandomIdsStatement.executeQuery();

            int[] ids = new int[count];
            int i = 0;
            while (result.next()) {
                ids[i] = result.getInt(LABEL_ID);
                i++;
            }

            if (i == count)
                emitter.onSuccess(ids);
            else
                emitter.onComplete();
        });
    }
}
