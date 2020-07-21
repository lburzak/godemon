package com.polydome.godemon.discordbot.view.service;

import io.reactivex.Completable;
import io.reactivex.Maybe;

public interface AsyncKeyValueCache <K, V> {
    Completable set(K key, V value);
    Maybe<V> get(K key);
    Completable remove(K key);
}