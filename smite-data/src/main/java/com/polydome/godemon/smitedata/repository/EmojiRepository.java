package com.polydome.godemon.smitedata.repository;

import com.polydome.godemon.smitedata.entity.Emoji;
import io.reactivex.Completable;

public interface EmojiRepository {
    Completable insert(Emoji emoji);
}
