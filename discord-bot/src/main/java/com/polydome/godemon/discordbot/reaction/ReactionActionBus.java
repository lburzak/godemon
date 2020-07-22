package com.polydome.godemon.discordbot.reaction;

import com.polydome.godemon.discordbot.view.action.Action;
import com.polydome.godemon.discordbot.view.action.MessageActionRegistry;
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
    private final ActionCodeStorage codeStorage;
    private final ActionArgStorage argStorage;
    private final Logger logger;
    private ActionListener listener;

    @Inject
    public ReactionActionBus(ActionCodeStorage codeStorage, ActionArgStorage argStorage, Logger logger) {
        this.codeStorage = codeStorage;
        this.argStorage = argStorage;
        this.logger = logger;
    }

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        if (event.getUser().isBot())
            return;

        long messageId = event.getMessageIdLong();

        Integer code = codeStorage.getCode(messageId);
        if (code == null)
            return;

        Action action;
        try {
            action = Action.getAction(code);
        } catch (NoSuchActionException e) {
            logger.warn("Invalid action code {} has been found on message {}, clearing message", code, messageId);
            codeStorage.clearCode(messageId);
            return;
        }

        switch (action) {
            case CREATE_CHALLENGE:
                listener.onCreateChallenge(event);
                break;
            case JOIN_CHALLENGE:
                listener.onJoinChallenge(event, argStorage.getIntArg(messageId, 0));
                break;
        }
    }

    @Override
    public void setActionArg(long messageId, int index, int arg) {
        argStorage.setIntArg(messageId, index, arg);
    }

    @Override
    public void setAction(long messageId, Action action) {
        codeStorage.setCode(messageId, Action.getCode(action));
    }

    @Override
    public void clearAction(long messageId) {
        codeStorage.clearCode(messageId);
    }

    public void setListener(ActionListener listener) {
        this.listener = listener;
    }
}
