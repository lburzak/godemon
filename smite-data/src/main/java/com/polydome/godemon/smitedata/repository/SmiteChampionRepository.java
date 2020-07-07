package com.polydome.godemon.smitedata.repository;

import com.polydome.godemon.smitedata.entity.SmiteChampion;
import io.reactivex.Maybe;

public interface SmiteChampionRepository {
    Maybe<SmiteChampion> findById(int id);
    Maybe<SmiteChampion> findByEmote(String emoteId);
    Maybe<int[]> getRandomIds(int count);
}
