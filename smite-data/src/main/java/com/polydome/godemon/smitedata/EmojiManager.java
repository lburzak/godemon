package com.polydome.godemon.smitedata;

import com.polydome.godemon.smitedata.endpoint.EmojiEndpoint;
import com.polydome.godemon.smitedata.entity.Emoji;
import com.polydome.godemon.smitedata.entity.EmojiHost;
import com.polydome.godemon.smitedata.entity.God;
import com.polydome.godemon.smitedata.entity.HostedEmoji;
import com.polydome.godemon.smitedata.repository.EmojiRepository;
import com.polydome.godemon.smitedata.repository.GodsRepository;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class EmojiManager {
    private final EmojiEndpoint emojiEndpoint;
    private final EmojiRepository emojiRepository;
    private final GodsRepository godsRepository;

    public EmojiManager(EmojiEndpoint emojiEndpoint, EmojiRepository emojiRepository, GodsRepository godsRepository) {
        this.emojiEndpoint = emojiEndpoint;
        this.emojiRepository = emojiRepository;
        this.godsRepository = godsRepository;
    }

    private String createEmojiDisplayName(String name, long id) {
        return String.format(":%s:%d", name, id);
    }

    private Single<Emoji> serializeHostedEmoji(HostedEmoji hostedEmoji, int hostId) {
        return Single.create(emitter -> godsRepository.findByName(hostedEmoji.name)
                .switchIfEmpty(Single.just(new God(null, null, null)))
                .subscribe(
                        god -> {
                            String displayName = createEmojiDisplayName(hostedEmoji.name, hostedEmoji.id);
                            emitter.onSuccess(new Emoji(god.id, displayName, hostedEmoji.id, hostId));
                        }
                ));
    }

    public Completable updateEmojiStorage(EmojiHost host) {
        return emojiEndpoint.fetchEmojisFromHost(host.guildId)
                .flatMapObservable(Observable::fromIterable)
                .flatMapSingle(emoji -> serializeHostedEmoji(emoji, host.id))
                .flatMapCompletable(emojiRepository::insert);
    }
}
