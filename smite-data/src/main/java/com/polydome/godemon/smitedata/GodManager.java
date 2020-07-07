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
                .replaceAll("'", "")
                .replaceAll(" ", "-");
    }

    public Completable updateKnownGods() {
        return godEndpoint.fetchLocalizedNamesOfAll()
                .flatMapObservable(Observable::fromIterable)
                .flatMapCompletable(localizedName -> godsRepository.insertIfNotExists(
                        new God(0, godNameFromLocalizedName(localizedName), localizedName)
                ));
    }
}
