package com.polydome.godemon.data.dao;

import com.polydome.godemon.data.dao.common.BaseDAO;
import com.polydome.godemon.smitedata.entity.God;
import com.polydome.godemon.smitedata.entity.SmiteChampion;
import com.polydome.godemon.smitedata.repository.GodsRepository;
import com.polydome.godemon.smitedata.repository.SmiteChampionRepository;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

import javax.sql.DataSource;
import java.sql.*;

public class GodDAO extends BaseDAO implements GodsRepository, SmiteChampionRepository {
    public GodDAO(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Maybe<God> findByName(String name) {
        return Maybe.create(emitter -> {
            withStatement("SELECT * FROM god WHERE name LIKE ? LIMIT 1", findByName -> {
                findByName.setString(1, name);

                try (ResultSet result = findByName.executeQuery()) {
                    if (result.next())
                        emitter.onSuccess(godFromRow(result));
                    else
                        emitter.onComplete();
                }
            });
        });
    }

    @Override
    public Completable insertIfNotExists(God god) {
        return Completable.create(emitter -> {
            withStatement("INSERT IGNORE INTO god(id, name, name_en) VALUES (?, ?, ?)", insertIfNotExists -> {
                insertIfNotExists.setInt(1, god.id);
                insertIfNotExists.setString(2, god.name);
                insertIfNotExists.setString(3, god.displayName);

                insertIfNotExists.execute();
                emitter.onComplete();

            });
        });
    }

    @Override
    public Single<Integer> countAll() {
        return Single.create(emitter -> {
            withStatement("SELECT COUNT(*) as count FROM god", countAll -> {
                try (ResultSet result = countAll.executeQuery()) {
                    emitter.onSuccess(result.getInt("count"));
                }
            });
        });
    }

    @Override
    public Maybe<SmiteChampion> findById(int id) {
        return Maybe.create(emitter -> {
            withStatement("SELECT god.id, god.name, god.name_en, emote.display_id FROM god INNER JOIN emote ON god.id = emote.god_id WHERE god.id = ?", findByIdWithEmote -> {
                findByIdWithEmote.setInt(1, id);

                try (ResultSet result = findByIdWithEmote.executeQuery()) {
                    if (result.next()) {
                        emitter.onSuccess(smiteChampionFromRow(result));
                    } else {
                        emitter.onComplete();
                    }
                }
            });
        });
    }

    @Override
    public Maybe<SmiteChampion> findByEmote(String emoteId) {
        return Maybe.create(emitter -> {
            withStatement("SELECT god.id, god.name, god.name_en, emote.display_id FROM god INNER JOIN emote ON god.id = emote.god_id WHERE emote.display_id LIKE ?", findByEmoteStatement -> {
                findByEmoteStatement.setString(1, emoteId);

                try (ResultSet result = findByEmoteStatement.executeQuery()) {
                    if (result.next()) {
                        emitter.onSuccess(smiteChampionFromRow(result));
                    } else {
                        emitter.onComplete();
                    }
                }
            });
        });
    }

    @Override
    public Maybe<int[]> getRandomIds(int count) {
        return Maybe.create(emitter -> {
            withStatement("SELECT id FROM god ORDER BY rand() limit ?", getRandomIdsStatement -> {
                getRandomIdsStatement.setInt(1, count);

                try (ResultSet result = getRandomIdsStatement.executeQuery()) {
                    int[] ids = new int[count];
                    int i = 0;
                    while (result.next()) {
                        ids[i] = result.getInt("id");
                        i++;
                    }

                    if (i == count)
                        emitter.onSuccess(ids);
                    else
                        emitter.onComplete();
                }
            });
        });
    }

    private SmiteChampion smiteChampionFromRow(ResultSet row) throws SQLException {
        return new SmiteChampion(
                row.getInt("god.id"),
                row.getString("god.name"),
                row.getString("god.name_en"),
                row.getString("emote.display_id")
        );
    }

    private God godFromRow(ResultSet row) throws SQLException {
        return new God(
                row.getInt("id"),
                row.getString("name"),
                row.getString("name_en")
        );
    }
}
