package com.polydome.godemon.discordbotapp;

import com.polydome.godemon.discordbot.emote.XmlEmoteStore;
import com.polydome.godemon.discordbot.listener.CommandListener;
import com.polydome.godemon.discordbot.reaction.ReactionActionBus;
import io.reactivex.Completable;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@Service
public class GodemonBot {
    private final JDA jda;
    private final CommandListener commandListener;
    private final ReactionActionBus reactionActionBus;
    private final XmlEmoteStore xmlEmoteStore;
    private final Logger logger;

    @Inject
    public GodemonBot(JDA jda, CommandListener commandListener, ReactionActionBus reactionActionBus, XmlEmoteStore xmlEmoteStore, Logger logger) {
        this.jda = jda;
        this.commandListener = commandListener;
        this.reactionActionBus = reactionActionBus;
        this.xmlEmoteStore = xmlEmoteStore;
        this.logger = logger;
    }

    public Completable init() {
        return Completable.fromRunnable(() -> {
            final String emotesFileUrl = "emotes.xml";
            try (final InputStream inputStream = new FileInputStream(emotesFileUrl)) {
                xmlEmoteStore.load(inputStream)
                        .doOnSuccess((emotes) ->
                                logger.info("Successfully loaded emotes from {}: {}", emotesFileUrl, emotes)
                        ).subscribe();
            } catch (FileNotFoundException e) {
                logger.warn("Missing emote definitions file {}!", emotesFileUrl);
            } catch (IOException e) {
                logger.warn("Opening emote definitions file {} failed!", emotesFileUrl);
            }
            jda.addEventListener(commandListener, reactionActionBus);
        });
    }

    public Completable shutdown() {
        return Completable.fromRunnable(jda::shutdown);
    }
}
