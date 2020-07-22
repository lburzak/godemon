package com.polydome.godemon.discordbot.view;

import com.polydome.godemon.discordbot.emote.EmoteManager;
import com.polydome.godemon.discordbot.view.action.Action;
import com.polydome.godemon.discordbot.view.action.MessageActionRegistry;
import com.polydome.godemon.discordbot.view.service.GodsDataProvider;
import com.polydome.godemon.domain.entity.GameMode;
import com.polydome.godemon.domain.model.ChallengeBrief;
import com.polydome.godemon.domain.model.ChallengeStatus;
import com.polydome.godemon.presentation.contract.ChallengeContract;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
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
        String messageContent = "";

        switch (notification) {
            case CHALLENGER_NOT_REGISTERED:
                messageContent = String.format("%s, please register with `;godemon me <SMITE_USERNAME>`", mention);
                break;
            case CHALLENGER_ALREADY_PARTICIPATES:
                messageContent = String.format("%s, you participate in this challenge already.", mention);
                break;
            case CHALLENGE_NOT_EXISTS:
                messageContent = String.format("%s, such challenge doesn't exist!", mention);
                break;
            case CHALLENGE_CREATED:
                messageContent = String.format("%s, challenge created.", mention);
                break;
            case CHALLENGER_NOT_PARTICIPATES:
                messageContent = String.format("%s, you do not participate in that challenge.", mention);
                break;
            case REGISTRATION_INVALID_HIREZNAME:
                messageContent = String.format("%s, there is no such SMITE player.", mention);
                break;
            case REGISTRATION_ALREADY_REGISTERED:
                messageContent = String.format("%s, you are already registered", mention);
                break;
            default:
                return;
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
    public void showIntermediateChallengeStatus(ChallengeStatus challengeStatus, int challengeId) {
        channel.sendMessage(embedFactory.challengeStatus(challengeStatus, true)).queue(msg -> {
            outMessage = msg;

            messageActionRegistry.setAction(msg.getIdLong(), Action.JOIN_CHALLENGE);
            messageActionRegistry.setActionArg(msg.getIdLong(), 0, challengeId);

            msg.addReaction(msg.getJDA().getEmoteById(735488600980062210L)).queue();
        });
    }

    @Override
    public void showFinalChallengeStatus(ChallengeStatus challengeStatus, int challengeId) {
        outMessage.editMessage(embedFactory.challengeStatus(challengeStatus, false)).queue();
        outMessage.addReaction(outMessage.getJDA().getEmoteById(735567114638852178L)).queue();
    }

    @Override
    public void showProposition(int challengeId, int[] godsIds) {
        String messageContent = String.format(
                "%s, choose your first god from the following:",
                mention
        );

        channel.sendMessage(messageContent).queue(message -> {
            outMessage = message;
            messageActionRegistry.setAction(message.getIdLong(), Action.ACCEPT_CHALLENGE);
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

    @Override
    public void showUpdating() {
        outMessage.clearReactions(outMessage.getJDA().getEmoteById(735567114638852178L)).queue();

        MessageEmbed embed = outMessage.getEmbeds().get(0);
        EmbedBuilder builder = new EmbedBuilder();

        builder.setTitle(embed.getTitle());
        for (final var field : embed.getFields())
            builder.addField(field);

        builder.setFooter("Updating...");

        outMessage.editMessage(builder.build()).queue();
    }

    @Override
    public void showLobby(List<ChallengeBrief> challenges) {
        channel.sendMessage(
                embedFactory.lobby(challenges)
        ).queue();
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

        public DiscordChallengeView create(MessageReceivedEvent event) {
            return create(event.getAuthor().getAsMention(), event.getChannel(), null);
        }
    }
}