package com.polydome.godemon.smitedata;

import com.polydome.godemon.smitedata.endpoint.EmoteEndpoint;
import com.polydome.godemon.smitedata.entity.Emote;
import com.polydome.godemon.smitedata.entity.EmoteHost;
import com.polydome.godemon.smitedata.entity.HostedEmote;
import com.polydome.godemon.smitedata.repository.EmoteHostRepository;
import com.polydome.godemon.smitedata.repository.EmoteRepository;
import com.polydome.godemon.smitedata.repository.GodsRepository;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class EmoteManager {
    private final EmoteEndpoint emoteEndpoint;
    private final EmoteRepository emoteRepository;
    private final GodsRepository godsRepository;
    private final EmoteHostRepository emoteHostRepository;
    private final Logger logger;

    public EmoteManager(EmoteEndpoint emoteEndpoint, EmoteRepository emoteRepository, GodsRepository godsRepository, EmoteHostRepository emoteHostRepository) {
        this.emoteEndpoint = emoteEndpoint;
        this.emoteRepository = emoteRepository;
        this.godsRepository = godsRepository;
        this.emoteHostRepository = emoteHostRepository;
        logger = LoggerFactory.getLogger(EmoteManager.class);
    }

    private String createEmoteDisplayName(String name, long id) {
        return String.format(":%s:%d", name, id);
    }

    private Single<Emote> serializeHostedEmote(HostedEmote hostedEmote, int hostId) {
        return Single.create(emitter -> godsRepository.findByName(hostedEmote.name)
                .doOnComplete(() -> logger.warn("God not found: {}", hostedEmote.name))
                .subscribe(
                        god -> {
                            String displayName = createEmoteDisplayName(hostedEmote.name, hostedEmote.id);
                            emitter.onSuccess(new Emote(god.id, displayName, hostedEmote.id, hostId));
                        }
                ));
    }

    private Completable updateKnownEmotesFromHost(EmoteHost host) {
        return emoteEndpoint.fetchEmotesFromHost(host.guildId)
                .doOnSuccess(emotes -> logger.info("Emotes found: {}", emotes.size()))
                .flatMapObservable(Observable::fromIterable)
                .flatMapSingle(emote -> serializeHostedEmote(emote, host.id))
                .doOnNext(emote -> logger.info("New emote `{}`", emote.displayId))
                .flatMapCompletable(emoteRepository::insert);
    }

    public Completable updateKnownEmotes() {
        return emoteHostRepository.findAll()
                .doOnNext(emoteHost -> logger.info("Processing emotes from {}", emoteHost.guildId))
                .flatMapCompletable(this::updateKnownEmotesFromHost);
    }
}
