package com.polydome.godemon.smiteapi.implementation;

import com.polydome.godemon.domain.usecase.PlayerEndpoint;
import com.polydome.godemon.smiteapi.client.SmiteApiClient;
import com.polydome.godemon.smiteapi.model.Player;
import io.reactivex.Single;

public class PlayerEndpointImpl implements PlayerEndpoint {
    private final SmiteApiClient smiteApiClient;

    public PlayerEndpointImpl(SmiteApiClient smiteApiClient) {
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
