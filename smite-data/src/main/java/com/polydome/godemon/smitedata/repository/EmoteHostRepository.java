package com.polydome.godemon.smitedata.repository;

import com.polydome.godemon.smitedata.entity.EmoteHost;
import io.reactivex.Observable;

public interface EmoteHostRepository {
    Observable<EmoteHost> findAll();
}
