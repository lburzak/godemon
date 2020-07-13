package com.polydome.godemon.smitedata.endpoint;

import io.reactivex.Single;

import java.util.List;

public interface GodEndpoint {
    Single<List<String>> fetchLocalizedNamesOfAll();
    Single<List<SmiteGod>> fetchAllGods();
}
