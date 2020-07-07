package com.polydome.godemon.smiteapi;

import com.polydome.godemon.smitedata.endpoint.GodEndpoint;
import io.reactivex.Observable;
import io.reactivex.Single;
import lombok.AllArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@AllArgsConstructor
public class GodsEndpointImpl implements GodEndpoint {
    private final SmiteApiClient apiClient;

    @Override
    public Single<List<String>> fetchLocalizedNamesOfAll() {
        return apiClient.getGods()
                .flatMapObservable(Observable::fromIterable)
                .map(GodDefinition::getName)
                .collectInto(new LinkedList<>(), List::add);
    }
}
