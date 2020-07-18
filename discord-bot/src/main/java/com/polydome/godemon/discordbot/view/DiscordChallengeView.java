package com.polydome.godemon.discordbot.view;

import com.polydome.godemon.discordbot.view.service.AsyncKeyValueCache;
import com.polydome.godemon.discordbot.view.service.GodsDataProvider;
import com.polydome.godemon.domain.model.ChallengeBrief;
import com.polydome.godemon.domain.model.ChallengeStatus;
import com.polydome.godemon.presentation.contract.ChallengeContract;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

public class DiscordChallengeView implements ChallengeContract.View {
    private final String mention;
    private final MessageChannel channel;
    private final @Named("PropositionCache") AsyncKeyValueCache cache;
    private final EmbedFactory embedFactory;
    private final GodsDataProvider godsDataProvider;
    private Message outMessage;

    public DiscordChallengeView(String mention, MessageChannel channel, AsyncKeyValueCache cache, EmbedFactory embedFactory, GodsDataProvider godsDataProvider, Message outMessage) {
        this.mention = mention;
        this.channel = channel;
        this.cache = cache;
        this.embedFactory = embedFactory;
        this.godsDataProvider = godsDataProvider;
        this.outMessage = outMessage;
    }

    @Override
    public void showChallengesList(List<ChallengeBrief> challenges) {
        channel.sendMessage(embedFactory.challengesList(challenges, mention)).queue();
    }

    @Override
    public void showRegistrationSuccess(String registeredName) {
        String msg = String.format("%s, You have been acknowledged as %s!", mention, registeredName);
        channel.sendMessage(msg).queue();
    }

    @Override
    public void showNotification(ChallengeContract.Notification notification) {
        String messageContent =
            switch (notification) {
                case CHALLENGER_NOT_REGISTERED -> String.format("%s, please register with `;godemon me <SMITE_USERNAME>`", mention);
                case CHALLENGER_ALREADY_PARTICIPATES -> String.format("%s, you participate in this challenge already.", mention);
                case CHALLENGE_NOT_EXISTS -> String.format("%s, such challenge doesn't exist!", mention);
                case CHALLENGE_CREATED -> String.format("%s, challenge created.", mention);
                case CHALLENGER_NOT_PARTICIPATES -> String.format("%s, you do not participate in that challenge.", mention);
                case REGISTRATION_INVALID_HIREZNAME -> String.format("%s, there is no such SMITE player.", mention);
                case REGISTRATION_ALREADY_REGISTERED -> String.format("%s, you are already registered", mention);
            };

        channel.sendMessage(messageContent).queue();
    }

    @Override
    public void showStartingGod(int godId) {
        String content = String.format(
                "%s, Your starting god is `%s`! Good luck!",
                mention, godsDataProvider.findById(godId).getEmoteId()
        );

        if (outMessage != null) {
            outMessage.editMessage(content).queue();
        }
    }

    @Override
    public void showChallengeStatus(ChallengeStatus challengeStatus) {
        channel.sendMessage(embedFactory.challengeStatus(challengeStatus)).queue();
    }

    @Override
    public void showProposition(int challengeId, int[] godsIds) {
        if (outMessage != null) {
            String messageContent = String.format(
                    "%s, choose your first god from the following:",
                    mention
            );

            outMessage.editMessage(messageContent).queue();

            channel.sendMessage(messageContent).queue(message -> {
                outMessage = message;
                cache.setLongToInt(message.getIdLong(), challengeId).subscribe();

                for (final int id : godsIds) {
                    message.addReaction(godsDataProvider.findById(id).getEmoteId()).queue();
                }
            });
        }
    }

    @Service
    public static class Factory {
        private final @Named("PropositionCache") AsyncKeyValueCache cache;
        private final EmbedFactory embedFactory;
        private final GodsDataProvider godsDataProvider;

        @Inject
        public Factory(AsyncKeyValueCache cache, EmbedFactory embedFactory, GodsDataProvider godsDataProvider) {
            this.cache = cache;
            this.embedFactory = embedFactory;
            this.godsDataProvider = godsDataProvider;
        }

        public DiscordChallengeView create(String authorMention, MessageChannel channel, Message outMessage) {
            return new DiscordChallengeView(authorMention, channel, cache, embedFactory, godsDataProvider, outMessage);
        }
    }
}