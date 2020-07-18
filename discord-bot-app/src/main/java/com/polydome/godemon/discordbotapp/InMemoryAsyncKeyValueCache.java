package com.polydome.godemon.discordbotapp;

import com.polydome.godemon.discordbot.view.service.AsyncKeyValueCache;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class InMemoryAsyncKeyValueCache implements AsyncKeyValueCache {
    Map<Long, Integer> data = new HashMap<>();

    @Override
    public Completable setLongToInt(long key, int value) {
        return Completable.fromRunnable(() -> data.put(key, value));
    }

    @Override
    public Maybe<Integer> getIntFromLong(long key) {
        return Maybe.create(emitter -> {
            if (!data.containsKey(key))
                emitter.onComplete();
            else {
                emitter.onSuccess(data.get(key));
            }
        });
    }
}
