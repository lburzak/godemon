package com.polydome.godemon.smitedata.implementation;

import com.polydome.godemon.smiteapi.client.SmiteApiClient;
import com.polydome.godemon.smiteapi.model.GodDefinition;
import com.polydome.godemon.smitedata.endpoint.GodEndpoint;
import com.polydome.godemon.smitedata.endpoint.SmiteGod;
import io.reactivex.Observable;
import io.reactivex.Single;
import lombok.AllArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@AllArgsConstructor
public class SmiteGodsEndpoint implements GodEndpoint {
    private final SmiteApiClient apiClient;

    @Override
    public Single<List<String>> fetchLocalizedNamesOfAll() {
        return apiClient.getGods()
                .flatMapObservable(Observable::fromIterable)
                .map(GodDefinition::getName)
                .collectInto(new LinkedList<>(), List::add);
    }

    @Override
    public Single<List<SmiteGod>> fetchAllGods() {
        return apiClient.getGods()
                .flatMapObservable(Observable::fromIterable)
                .map(definition -> new SmiteGod(definition.getId(), definition.getName()))
                .collectInto(new LinkedList<>(), List::add);
    }
}
