package com.polydome.godemon.smitedata;

import com.polydome.godemon.smitedata.endpoint.EmoteEndpoint;
import com.polydome.godemon.smitedata.entity.Emote;
import com.polydome.godemon.smitedata.entity.EmoteHost;
import com.polydome.godemon.smitedata.entity.God;
import com.polydome.godemon.smitedata.entity.HostedEmote;
import com.polydome.godemon.smitedata.repository.EmoteRepository;
import com.polydome.godemon.smitedata.repository.GodsRepository;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class EmoteManager {
    private final EmoteEndpoint emoteEndpoint;
    private final EmoteRepository emoteRepository;
    private final GodsRepository godsRepository;

    public EmoteManager(EmoteEndpoint emoteEndpoint, EmoteRepository emoteRepository, GodsRepository godsRepository) {
        this.emoteEndpoint = emoteEndpoint;
        this.emoteRepository = emoteRepository;
        this.godsRepository = godsRepository;
    }

    private String createEmoteDisplayName(String name, long id) {
        return String.format(":%s:%d", name, id);
    }

    private Single<Emote> serializeHostedEmote(HostedEmote hostedEmote, int hostId) {
        return Single.create(emitter -> godsRepository.findByName(hostedEmote.name)
                .switchIfEmpty(Single.just(new God(null, null, null)))
                .subscribe(
                        god -> {
                            String displayName = createEmoteDisplayName(hostedEmote.name, hostedEmote.id);
                            emitter.onSuccess(new Emote(god.id, displayName, hostedEmote.id, hostId));
                        }
                ));
    }

    public Completable updateEmoteStorage(EmoteHost host) {
        return emoteEndpoint.fetchEmotesFromHost(host.guildId)
                .flatMapObservable(Observable::fromIterable)
                .flatMapSingle(emote -> serializeHostedEmote(emote, host.id))
                .flatMapCompletable(emoteRepository::insert);
    }
}
