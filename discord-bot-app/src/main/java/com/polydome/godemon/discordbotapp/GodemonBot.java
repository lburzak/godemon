package com.polydome.godemon.discordbotapp;

import com.polydome.godemon.discordbot.listener.CommandListener;
import com.polydome.godemon.discordbot.reaction.ReactionActionBus;
import com.polydome.godemon.smiteapi.client.SmiteApiClient;
import io.reactivex.Completable;
import net.dv8tion.jda.api.JDA;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class GodemonBot {
    private final JDA jda;
    private final CommandListener commandListener;
    private final ReactionActionBus reactionActionBus;

    @Inject
    public GodemonBot(JDA jda, CommandListener commandListener, ReactionActionBus reactionActionBus) {
        this.jda = jda;
        this.commandListener = commandListener;
        this.reactionActionBus = reactionActionBus;
    }

    public Completable init() {
        return Completable.fromRunnable(() -> jda.addEventListener(commandListener, reactionActionBus));
    }

    public Completable shutdown() {
        return Completable.fromRunnable(jda::shutdown);
    }
}
