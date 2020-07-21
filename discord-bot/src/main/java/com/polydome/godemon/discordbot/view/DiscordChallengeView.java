package com.polydome.godemon.discordbot.view;

import com.polydome.godemon.discordbot.emote.EmoteManager;
import com.polydome.godemon.discordbot.view.action.Action;
import com.polydome.godemon.discordbot.view.action.MessageActionRegistry;
import com.polydome.godemon.discordbot.view.service.GodsDataProvider;
import com.polydome.godemon.domain.entity.GameMode;
import com.polydome.godemon.domain.model.ChallengeBrief;
import com.polydome.godemon.domain.model.ChallengeStatus;
import com.polydome.godemon.presentation.contract.ChallengeContract;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

public class DiscordChallengeView implements ChallengeContract.View {
    private final String mention;
    private final MessageChannel channel;
    private final EmoteManager emoteManager;
    private final MessageActionRegistry messageActionRegistry;
    private final EmbedFactory embedFactory;
    private final GodsDataProvider godsDataProvider;
    private Message outMessage;

    public DiscordChallengeView(String mention, MessageChannel channel, EmoteManager emoteManager, MessageActionRegistry messageActionRegistry, EmbedFactory embedFactory, GodsDataProvider godsDataProvider, Message outMessage) {
        this.mention = mention;
        this.channel = channel;
        this.emoteManager = emoteManager;
        this.messageActionRegistry = messageActionRegistry;
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
                mention, godsDataProvider.findById(godId).getName()
        );

        if (outMessage != null) {
            outMessage.editMessage(content).queue();
            outMessage.clearReactions().queue();
        }
    }

    @Override
    public void showChallengeStatus(ChallengeStatus challengeStatus) {
        channel.sendMessage(embedFactory.challengeStatus(challengeStatus)).queue();
    }

    @Override
    public void showProposition(int challengeId, int[] godsIds) {
        String messageContent = String.format(
                "%s, choose your first god from the following:",
                mention
        );

        channel.sendMessage(messageContent).queue(message -> {
            outMessage = message;
            messageActionRegistry.setAction(message.getIdLong(), Action.JOIN_CHALLENGE);
            messageActionRegistry.setActionArg(message.getIdLong(),0, challengeId);

            for (final int id : godsIds) {
                message.addReaction(godsDataProvider.findById(id).getEmoteId()).queue();
            }
        });
    }

    @Override
    public void showModeChoice(Set<GameMode> modes) {
        StringBuilder builder = new StringBuilder();
        builder.append("Select queue:\n");

        for (byte i = 0; i <= Queue.lastIndex; i++)
            builder.append(String.format("%s %s\n", emoteManager.fromDigit(i), Queue.fromIndex(i).getVisibleName()));

        channel.sendMessage(builder.toString()).queue(msg -> {
            for (byte i = 0; i <= Queue.lastIndex; i++) {
                msg.addReaction(emoteManager.fromDigit(i)).queue();
            }

            messageActionRegistry.setAction(msg.getIdLong(), Action.CREATE_CHALLENGE);
        });
    }

    @Service
    public static class Factory {
        private final EmbedFactory embedFactory;
        private final GodsDataProvider godsDataProvider;
        private final EmoteManager emoteManager;
        private final MessageActionRegistry messageActionRegistry;

        @Inject
        public Factory(EmbedFactory embedFactory, GodsDataProvider godsDataProvider, EmoteManager emoteManager, MessageActionRegistry messageActionRegistry) {
            this.embedFactory = embedFactory;
            this.godsDataProvider = godsDataProvider;
            this.emoteManager = emoteManager;
            this.messageActionRegistry = messageActionRegistry;
        }

        public DiscordChallengeView create(String authorMention, MessageChannel channel, Message outMessage) {
            return new DiscordChallengeView(authorMention, channel, emoteManager, messageActionRegistry, embedFactory, godsDataProvider, outMessage);
        }
    }
}