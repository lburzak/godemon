package com.polydome.godemon.discordbot.listener;

import com.polydome.godemon.discordbot.emote.EmoteManager;
import com.polydome.godemon.discordbot.reaction.ActionListener;
import com.polydome.godemon.discordbot.reaction.MessageActionRegistry;
import com.polydome.godemon.discordbot.view.DiscordChallengeView;
import com.polydome.godemon.discordbot.view.Queue;
import com.polydome.godemon.domain.entity.GameMode;
import com.polydome.godemon.presentation.contract.ChallengeContract;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class MessageActionListener implements ActionListener {
    private final ChallengeContract.Presenter presenter;
    private final DiscordChallengeView.Factory challengeViewFactory;
    private final EmoteManager emoteManager;
    private final MessageActionRegistry messageActionRegistry;

    @Inject
    public MessageActionListener(ChallengeContract.Presenter presenter, DiscordChallengeView.Factory challengeViewFactory, EmoteManager emoteManager, MessageActionRegistry messageActionRegistry) {
        this.presenter = presenter;
        this.challengeViewFactory = challengeViewFactory;
        this.emoteManager = emoteManager;
        this.messageActionRegistry = messageActionRegistry;
    }

    public void onCreateChallenge(MessageReactionAddEvent event) {
        short digit;
        try {
             digit = emoteManager.toDigit(event.getReactionEmote());
        } catch (IllegalArgumentException e) {
            return;
        }

        event.retrieveMessage().queue(message -> {
            presenter.onModeChoice(
                    challengeViewFactory.create(event.getUser().getAsMention(), event.getChannel(), message),
                    event.getUserIdLong(),
                    Queue.fromIndex(digit).toGameMode()
            );

            messageActionRegistry.clearAction(event.getMessageIdLong());
        });
    }
}
