package com.polydome.godemon.smitedata.implementation;

import com.polydome.godemon.domain.service.PlayerEndpoint;
import com.polydome.godemon.smiteapi.client.SmiteApiClient;
import com.polydome.godemon.smiteapi.model.Player;
import io.reactivex.Single;

public class SmitePlayerEndpoint implements PlayerEndpoint {
    private final SmiteApiClient smiteApiClient;

    public SmitePlayerEndpoint(SmiteApiClient smiteApiClient) {
        this.smiteApiClient = smiteApiClient;
    }

    @Override
    public Integer fetchPlayerId(String name) {
        int id = smiteApiClient.getPlayer(name)
                .switchIfEmpty(Single.just(new Player(0, "")))
                .map(player -> player.id)
                .blockingGet();

        return (id == 0) ? null : id;
    }
}
