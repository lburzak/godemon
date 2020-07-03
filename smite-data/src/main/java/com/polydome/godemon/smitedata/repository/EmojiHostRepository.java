package com.polydome.godemon.smitedata.repository;

import com.polydome.godemon.smitedata.entity.EmojiHost;
import io.reactivex.Single;

import java.util.List;

public interface EmojiHostRepository {
    Single<List<EmojiHost>> findAll();
}
