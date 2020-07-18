package com.polydome.godemon.discordbot.view.service;

import io.reactivex.Completable;
import io.reactivex.Maybe;

public interface AsyncKeyValueCache {
    Completable setLongToInt(long key, int value);
    Maybe<Integer> getIntFromLong(long key);
}
