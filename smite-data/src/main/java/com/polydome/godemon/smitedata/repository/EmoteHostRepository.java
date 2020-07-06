package com.polydome.godemon.smitedata.repository;

import com.polydome.godemon.smitedata.entity.EmoteHost;
import io.reactivex.Single;

import java.util.List;

public interface EmoteHostRepository {
    Single<List<EmoteHost>> findAll();
}
