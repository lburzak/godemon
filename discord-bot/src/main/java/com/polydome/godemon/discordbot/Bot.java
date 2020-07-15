package com.polydome.godemon.discordbot;

import com.polydome.godemon.domain.entity.GameMode;
import com.polydome.godemon.domain.model.ChallengeProposition;
import com.polydome.godemon.domain.repository.*;
import com.polydome.godemon.domain.service.ChallengeService;
import com.polydome.godemon.domain.service.GameRulesProvider;
import com.polydome.godemon.domain.service.PlayerEndpoint;
import com.polydome.godemon.domain.service.matchdetails.MatchDetailsEndpoint;
import com.polydome.godemon.domain.usecase.*;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class Bot extends ListenerAdapter {
    private final ChallengerRepository challengerRepository;
    private final ChallengeRepository challengeRepository;
    private final PropositionRepository propositionRepository;
    private final GameRulesProvider gameRulesProvider;
    private final GodsDataProvider godsDataProvider;
    private final ChampionRepository championRepository;
    private final PlayerEndpoint playerEndpoint;
    private final MatchRepository matchRepository;
    private final MatchDetailsEndpoint matchDetailsEndpoint;
    private final ChallengeService challengeService;

    private static class CommandInvocation {

        public String command;
        public String[] args;

        public CommandInvocation(String command, String[] args) {
            this.command = command;
            this.args = args;
        }
    }

    private static CommandInvocation parseMessage(String message, String prefix) {
        if (message.startsWith(prefix)) {
            String[] invocation = message.substring(prefix.length() + 1).split(" ");

            String[] args = new String[invocation.length - 1];
            if (invocation.length - 1 >= 0) System.arraycopy(invocation, 1, args, 0, invocation.length - 1);

            return new CommandInvocation(invocation[0], args);
        } else {
            return null;
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot())
            return;

        Message msg = event.getMessage();
        CommandInvocation commandInvocation = parseMessage(msg.getContentRaw(), ";godemon");

        if (commandInvocation == null) {
            System.out.println("Unable to parse: " + msg.getContentRaw());
            return;
        }

        switch (commandInvocation.command) {
            case "challenge" -> onChallengeStatusRequested(event);
            case "me" -> onIntroduction(event, commandInvocation.args);
            case "request" -> onChallengeRequested(event);
            case "gods" -> onAvailableGodsRequested(event);
        }
    }

    private String createGodLabel(GodData godData, int usesLeft) {
        return String.format("`%dx` <%s> **%s**", usesLeft, godData.getEmoteId(), godData.getName());
    }

    private void onAvailableGodsRequested(MessageReceivedEvent event) {
        GetAvailableGodsUseCase getAvailableGodsUseCase = new GetAvailableGodsUseCase(challengeRepository, propositionRepository, challengerRepository);
        GetAvailableGodsUseCase.Result result = getAvailableGodsUseCase.execute(event.getAuthor().getIdLong());

        if (result.getError() == null) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            StringBuilder stringBuilder = new StringBuilder();
            GodData godData;

            for (var entry : result.getGodsToUsesLeft().entrySet()) {
                godData = godsDataProvider.findById(entry.getKey());
                stringBuilder.append(createGodLabel(godData, entry.getValue())).append("\n");
            }

            embedBuilder.setTitle(String.format("%s's gods", event.getAuthor().getName()));
            embedBuilder.setDescription(stringBuilder.toString());

            event.getChannel().sendMessage(embedBuilder.build())
                .queue();
        } else {
            System.err.println(result.getError());
        }
    }

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        if (!event.getReaction().isSelf())
            onChallengeAccepted(event);
    }

    private void onChallengeStatusRequested(MessageReceivedEvent event) {
        MessageChannel channel = event.getChannel();

        GetChallengeStatusUseCase getChallengeStatusUseCase = new GetChallengeStatusUseCase(challengerRepository, challengeRepository, challengeService);
        GetChallengeStatusUseCase.Result result = getChallengeStatusUseCase.execute(event.getAuthor().getIdLong());

        String message;

        if (result.status != null) {
            message = result.status.toString();
        } else {
            message = switch (result.error) {
                case CHALLENGER_NOT_REGISTERED -> "A Challenge? Don't you think I deserve a proper introduction first?";
                case CHALLENGE_NOT_ACTIVE -> "You have no active challenge. Do you want to begin?";
            };
        }

        channel.sendMessage(message).queue();
    }

    private void onIntroduction(MessageReceivedEvent event, String[] args) {
        MessageChannel channel = event.getChannel();

        IntroduceUseCase introduceUseCase = new IntroduceUseCase(challengerRepository, playerEndpoint);
        IntroduceUseCase.Result result = introduceUseCase.execute(event.getAuthor().getIdLong(), args[0]);

        String message;

        if (result.getError() == null) {
            message = String.format("%s, You have been acknowledged as %s!", event.getAuthor().getAsMention(), result.getNewName());
        } else {
            message = switch (result.getError()) {
                case CHALLENGER_ALREADY_REGISTERED -> "I already know you...";
                case PLAYER_NOT_EXISTS -> String.format("There is no SMITE player named %s", args[0]);
            };
        }

        channel.sendMessage(message).queue();
    }

    private void onChallengeRequested(MessageReceivedEvent event) {
        StartChallengeUseCase startChallengeUseCase = new StartChallengeUseCase(
                challengerRepository,
                challengeRepository
        );

        startChallengeUseCase.execute(event.getAuthor().getIdLong(), GameMode.RANKED_DUEL);

        event.getChannel().sendMessage("Challenge created!").queue();
    }

    private void onChallengeJoin(MessageReceivedEvent event, String[] args) {
        JoinChallengeUseCase joinChallengeUseCase = new JoinChallengeUseCase(challengeService, challengeRepository, challengerRepository, championRepository, gameRulesProvider, propositionRepository);

        event.getChannel().sendMessage("I'm picking some gods for you...").queue(
                message -> {
                    ChallengeProposition proposition = joinChallengeUseCase.withChallengeId(event.getAuthor().getIdLong(), Integer.parseInt(args[0]), message.getIdLong());

                    String content;

                    if (proposition != null) {
                        content = String.format(
                                "%s, choose your first god from the following:",
                                event.getAuthor().getAsMention()
                        );

                        List<GodData> godsData = Arrays.stream(proposition.getGods())
                                .mapToObj(godsDataProvider::findById)
                                .collect(Collectors.toList());

                        message.editMessage(content).queue(sentMessage -> {
                            for (GodData godData : godsData) {
                                sentMessage.addReaction(godData.getEmoteId()).queue();
                            }
                        });
                    }
                }
        );
    }

    private void onChallengeAccepted(MessageReactionAddEvent event) {
        AcceptChallengeUseCase acceptChallengeUseCase = new AcceptChallengeUseCase(
                challengerRepository,
                challengeRepository,
                propositionRepository
        );

        event.retrieveMessage().queue(message -> {
            System.out.println(message.getContentRaw());

            String emoteId = String.format(":%s:%s", event.getReactionEmote().getName(), event.getReactionEmote().getId());
            GodData godData = godsDataProvider.findByEmote(emoteId);
            if (godData == null) {
                System.out.println("God not found: " + emoteId);
                return;
            }
            AcceptChallengeUseCase.Result result =
                    acceptChallengeUseCase.execute(event.getUserIdLong(), event.getMessageIdLong(), godData.getId());

            String content;

            if (result.getError() == null) {
                content = String.format(
                        "%s, Your starting god is `%s`! Good luck!",
                        event.getUser().getAsMention(), godData.getName()
                );
                message.editMessage(content).queue();

                message.clearReactions().queue();
            } else {
                System.err.println("Challenge already active");
            }
        });
    }

}
