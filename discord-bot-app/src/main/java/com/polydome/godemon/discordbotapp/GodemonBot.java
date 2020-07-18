package com.polydome.godemon.discordbotapp;

import com.polydome.godemon.smiteapi.client.SmiteApiClient;
import io.reactivex.Completable;
import net.dv8tion.jda.api.JDA;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class GodemonBot {
    private final JDA jda;
    private final SmiteApiClient smiteApiClient;

    @Inject
    public GodemonBot(JDA jda, SmiteApiClient smiteApiClient) {
        this.jda = jda;
        this.smiteApiClient = smiteApiClient;
    }

    public Completable init() {
        return smiteApiClient.initialize();
    }

    public Completable shutdown() {
        return Completable.fromRunnable(jda::shutdown);
    }
}
