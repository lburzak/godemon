package com.polydome.godemon.smitedata.repository;

import com.polydome.godemon.smitedata.entity.Emote;
import io.reactivex.Completable;

public interface EmoteRepository {
    Completable insert(Emote emote);
}
