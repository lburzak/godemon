package com.polydome.godemon.data.dao;

import com.polydome.godemon.data.dao.common.BaseDAO;
import com.polydome.godemon.smitedata.entity.Emote;
import com.polydome.godemon.smitedata.repository.EmoteRepository;
import io.reactivex.Completable;

import javax.sql.DataSource;
import java.sql.PreparedStatement;

public class EmoteDAO extends BaseDAO implements EmoteRepository {
    public EmoteDAO(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Completable insert(Emote emote) {
        return Completable.create(emitter -> {
            withStatement("INSERT IGNORE INTO emote(god_id, display_id, hosted_id, host_id) VALUES (?, ?, ?, ?)", insertEmote -> {
                insertEmote.setInt(1, emote.godId);
                insertEmote.setString(2, emote.displayId);
                insertEmote.setLong(3, emote.hostedId);
                insertEmote.setInt(4, emote.hostId);

                insertEmote.execute();

                emitter.onComplete();
            });
        });
    }
}
