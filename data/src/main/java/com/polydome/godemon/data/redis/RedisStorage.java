package com.polydome.godemon.data.redis;

import com.polydome.godemon.smiteapi.client.SessionStorage;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

public class RedisStorage implements SessionStorage {
    private final Jedis jedis;

    public RedisStorage(Jedis jedis) {
        this.jedis = jedis;
    }

    @Override
    public void setSessionId(String id, int expirationMinutes) {
        jedis.set("smiteapi:session", id, SetParams.setParams().ex(60 * expirationMinutes));
    }

    @Override
    public String getSessionId() {
        return jedis.get("smiteapi:session");
    }

    @Override
    public boolean existsSession() {
        return jedis.exists("smiteapi:session");
    }
}
