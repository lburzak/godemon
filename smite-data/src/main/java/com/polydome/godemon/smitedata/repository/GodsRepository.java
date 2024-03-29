package com.polydome.godemon.smitedata.repository;

import com.polydome.godemon.smitedata.entity.God;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

public interface GodsRepository {
    Maybe<God> findByName(String name);
    Completable insertIfNotExists(God god);
    Single<Integer> countAll();
}
