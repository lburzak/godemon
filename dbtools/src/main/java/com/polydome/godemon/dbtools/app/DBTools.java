package com.polydome.godemon.dbtools.app;

import com.polydome.godemon.smiteapi.client.SmiteApiClient;
import com.polydome.godemon.smitedata.EmoteManager;
import com.polydome.godemon.smitedata.GodManager;
import com.polydome.godemon.smitedata.endpoint.EmoteHostNotAvailableException;
import io.reactivex.Completable;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class DBTools {
    private final Logger logger;
    private final SmiteApiClient smiteApiClient;
    private final JDA jda;
    private final GodManager godManager;
    private final EmoteManager emoteManager;

    @Inject
    public DBTools(Logger logger, SmiteApiClient smiteApiClient, JDA jda, GodManager godManager, EmoteManager emoteManager) {
        this.logger = logger;
        this.smiteApiClient = smiteApiClient;
        this.jda = jda;
        this.godManager = godManager;
        this.emoteManager = emoteManager;
    }

    public Completable populateDatabase() {
        return smiteApiClient.initialize()
                .andThen(godManager.updateKnownGods())
                .andThen(emoteManager.updateKnownEmotes())
                .andThen(Completable.fromRunnable(jda::shutdownNow))
                .doOnError(e -> {
                    if (e instanceof EmoteHostNotAvailableException)
                        logger.warn("Host unavailable: `{}`", ((EmoteHostNotAvailableException) e).getGuildId());
                });
    }
}
