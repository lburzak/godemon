package com.polydome.godemon.discordbot.reaction;

import com.polydome.godemon.discordbot.listener.MessageActionListener;
import com.polydome.godemon.discordbot.view.service.AsyncKeyValueCache;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Service
public class ReactionActionBus extends ListenerAdapter implements MessageActionRegistry {
    private final AsyncKeyValueCache<Long, Integer> cache;
    private final Logger logger;
    private ActionListener listener;

    @Inject
    public ReactionActionBus(AsyncKeyValueCache<Long, Integer> cache, Logger logger) {
        this.cache = cache;
        this.logger = logger;
    }

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        long messageId = event.getMessageIdLong();
        cache.get(messageId).subscribe(code -> {
            Action action;

            try {
                action = Action.getAction(code);
            } catch (NoSuchActionException e) {
                logger.warn("Invalid action code has been found on message {}, removing", messageId);
                cache.remove(messageId).subscribe();
                return;
            }

            switch (action) {
                case CREATE_CHALLENGE -> listener.onCreateChallenge(event);
            }
        });
    }

    @Override
    public void setAction(long messageId, Action action) {
        cache.set(messageId, Action.getCode(action)).subscribe();
    }

    @Override
    public void clearAction(long messageId) {
        cache.remove(messageId).subscribe();
    }

    public void setListener(ActionListener listener) {
        this.listener = listener;
    }
}
