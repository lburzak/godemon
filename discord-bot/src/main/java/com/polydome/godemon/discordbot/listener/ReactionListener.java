package com.polydome.godemon.discordbot.listener;

import com.polydome.godemon.discordbot.view.DiscordChallengeView;
import com.polydome.godemon.discordbot.view.service.GodsDataProvider;
import com.polydome.godemon.discordbot.view.service.AsyncKeyValueCache;
import com.polydome.godemon.presentation.contract.ChallengeContract;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

@Service
public class ReactionListener extends ListenerAdapter {
    @Named("PropositionCache")
    private final AsyncKeyValueCache cache;
    private final ChallengeContract.Presenter presenter;
    private final GodsDataProvider godsDataProvider;
    private final DiscordChallengeView.Factory challengeViewFactory;

    @Inject
    public ReactionListener(AsyncKeyValueCache cache, ChallengeContract.Presenter presenter, GodsDataProvider godsDataProvider, DiscordChallengeView.Factory challengeViewFactory) {
        this.cache = cache;
        this.presenter = presenter;
        this.godsDataProvider = godsDataProvider;
        this.challengeViewFactory = challengeViewFactory;
    }

    String emoteIdFromEmote(MessageReaction.ReactionEmote emote) {
        return String.format(":%s:%s", emote.getName(), emote.getId());
    }

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        if (event.getReaction().isSelf())
            return;

        cache.getIntFromLong(event.getMessageIdLong()).subscribe(challengeId -> {
            event.retrieveMessage().queue(outMessage ->
                    presenter.onGodChoice(
                            challengeViewFactory.create(event.getUser().getAsMention(), event.getChannel(), outMessage),
                            event.getUserIdLong(),
                            challengeId,
                            godsDataProvider.findByEmote(emoteIdFromEmote(event.getReactionEmote())).getId())
            );
        });
    }
}
