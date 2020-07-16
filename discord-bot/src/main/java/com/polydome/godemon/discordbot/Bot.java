package com.polydome.godemon.discordbot;

import com.polydome.godemon.domain.entity.GameMode;
import com.polydome.godemon.domain.exception.ActionForbiddenException;
import com.polydome.godemon.domain.exception.AuthenticationException;
import com.polydome.godemon.domain.model.ChallengeBrief;
import com.polydome.godemon.domain.model.ChallengeProposition;
import com.polydome.godemon.domain.model.ChallengeStatus;
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
            case "challenge" -> {
                if (commandInvocation.args.length == 0)
                    onChallengesListRequested(event);
                else if (commandInvocation.args.length == 1) {
                    // TODO: Validate argument
                    onChallengeStatusRequested(event, Integer.parseInt(commandInvocation.args[0]));
                } else {
                    throw new UnsupportedOperationException("Invalid arguments count, expected 0 or 1");
                }
            }
            case "me" -> onIntroduction(event, commandInvocation.args);
            case "request" -> onChallengeRequested(event);
            case "gods" -> onAvailableGodsRequested(event);
            case "join" -> onChallengeJoin(event, commandInvocation.args);
        }
    }

    private String createGodLabel(GodData godData, int usesLeft) {
        return String.format("`%dx` <%s> **%s**", usesLeft, godData.getEmoteId(), godData.getName());
    }

    private void onAvailableGodsRequested(MessageReceivedEvent event) {
        GetAvailableGodsUseCase getAvailableGodsUseCase = new GetAvailableGodsUseCase(challengeRepository, propositionRepository, challengerRepository);
        GetAvailableGodsUseCase.Result result = getAvailableGodsUseCase.execute(event.getAuthor().getIdLong(), 1);

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

    private void onChallengeStatusRequested(MessageReceivedEvent event, int challengeId) {
        MessageChannel channel = event.getChannel();

        GetChallengeStatusUseCase getChallengeStatusUseCase = new GetChallengeStatusUseCase(challengerRepository, challengeRepository, challengeService);
        ChallengeStatus status = getChallengeStatusUseCase.execute(event.getAuthor().getIdLong(), challengeId);

        channel.sendMessage(status.toString()).queue();
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

        event.getChannel().sendMessage("Please wait...").queue(
                message -> {
                    ChallengeProposition proposition;

                    try {
                        proposition = joinChallengeUseCase.withChallengeId(event.getAuthor().getIdLong(), Integer.parseInt(args[0]), message.getIdLong());
                    } catch (ActionForbiddenException e) {
                        String messageContent = String.format("%s, you participate in this challenge already.", event.getAuthor().getAsMention());
                        message.editMessage(messageContent).queue();
                        return;
                    } catch (AuthenticationException e) {
                        String messageContent = String.format("%s, please register with `;godemon me <SMITE_USERNAME>`", event.getAuthor().getAsMention());
                        message.editMessage(messageContent).queue();
                        return;
                    }

                    if (proposition != null) {
                        String messageContent = String.format(
                                "%s, choose your first god from the following:",
                                event.getAuthor().getAsMention()
                        );

                        List<GodData> godsData = Arrays.stream(proposition.getGods())
                                .mapToObj(godsDataProvider::findById)
                                .collect(Collectors.toList());

                        message.editMessage(messageContent).queue(sentMessage -> {
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

            acceptChallengeUseCase.execute(event.getUserIdLong(), event.getMessageIdLong(), godData.getId());

            String content;

            content = String.format(
                    "%s, Your starting god is `%s`! Good luck!",
                    event.getUser().getAsMention(), godData.getName()
            );
            message.editMessage(content).queue();

            message.clearReactions().queue();
        });
    }

    private String createChallengeLabel(ChallengeBrief challengeBrief) {
        return String.format("%d   %s", challengeBrief.getId(), challengeBrief.getLastUpdate().toString());
    }

    private void onChallengesListRequested(MessageReceivedEvent event) {
        List<ChallengeBrief> challenges =
                (new GetAvailableChallengesUseCase(challengeRepository)).withChallengerId(event.getAuthor().getIdLong());

        StringBuilder contentBuilder = new StringBuilder();

        for (final var challenge : challenges) {
            contentBuilder.append(createChallengeLabel(challenge)).append("\n");
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder
                .setTitle(String.format("%s's challenges", event.getAuthor().getName()))
                .setDescription(contentBuilder.toString());

        event.getChannel().sendMessage(embedBuilder.build()).queue();
    }

}
