package com.polydome.godemon.data.dao;

import com.polydome.godemon.data.dao.common.BaseDAO;
import com.polydome.godemon.smitedata.entity.EmoteHost;
import com.polydome.godemon.smitedata.repository.EmoteHostRepository;
import io.reactivex.Observable;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmoteHostDAO extends BaseDAO implements EmoteHostRepository {
    public EmoteHostDAO(DataSource dataSource) {
        super(dataSource);
    }

    private EmoteHost emoteHostFromRow(ResultSet row) throws SQLException {
        return new EmoteHost(
                row.getInt("id"),
                row.getLong("guild_id")
        );
    }

    @Override
    public Observable<EmoteHost> findAll() {
        return Observable.create(emitter -> {
            withStatement("SELECT * FROM emote_host", findAllEmoteHosts -> {
                ResultSet result = findAllEmoteHosts.executeQuery();

                while (result.next()) {
                    emitter.onNext(emoteHostFromRow(result));
                }

                emitter.onComplete();
            });
        });
    }
}
