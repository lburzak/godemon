package com.polydome.godemon.smiteapi;

import com.polydome.godemon.domain.usecase.PlayerEndpoint;

public class PlayerEndpointImpl implements PlayerEndpoint {
    private final SmiteApiClient smiteApiClient;

    public PlayerEndpointImpl(SmiteApiClient smiteApiClient) {
        this.smiteApiClient = smiteApiClient;
    }

    @Override
    public Integer fetchPlayerId(String name) {
        return smiteApiClient.getPlayer(name).map(player -> player.id)
                .blockingGet();
    }
}
