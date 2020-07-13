package com.polydome.godemon.smitedata;

import com.polydome.godemon.smitedata.endpoint.GodEndpoint;
import com.polydome.godemon.smitedata.entity.God;
import com.polydome.godemon.smitedata.repository.GodsRepository;
import io.reactivex.Completable;
import io.reactivex.Observable;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GodManager {
    private final GodEndpoint godEndpoint;
    private final GodsRepository godsRepository;

    private String godNameFromLocalizedName(String localized) {
        return localized
                .toLowerCase()
                .replaceAll("[' ]", "");
    }

    public Completable updateKnownGods() {
        return godEndpoint.fetchAllGods()
                .flatMapObservable(Observable::fromIterable)
                .flatMapCompletable(smiteGod -> godsRepository.insertIfNotExists(
                        new God(smiteGod.getId(), godNameFromLocalizedName(smiteGod.getLocalizedName()), smiteGod.getLocalizedName())
                ));
    }
}
