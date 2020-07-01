package com.polydome.godemon.discordbot;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import com.polydome.godemon.domain.repository.ChallengerRepository;
import com.polydome.godemon.domain.usecase.*;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.Map;

public class Bot extends ListenerAdapter {
    private final ChallengerRepository challengerRepository = createChallengerRepositoryStub();
    private final ChallengeRepository challengeRepository = createChallengeRepositoryStub();
    private final GameRulesProvider gameRulesProvider = createGameRulesProviderStub();

    public static void main(String[] args) throws LoginException {
        if (args.length < 1) {
            System.out.println("You have to provide a token as first argument!");
            System.exit(1);
        }

        JDABuilder.createLight(args[0], GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES)
                .addEventListeners(new Bot())
                .build();
    }

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
            case "accept" -> onChallengeAccepted(event);
        }
    }

    private void onChallengeStatusRequested(MessageReceivedEvent event) {
        MessageChannel channel = event.getChannel();

        GetChallengeStatusUseCase getChallengeStatusUseCase = new GetChallengeStatusUseCase(challengerRepository, challengeRepository);
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

        IntroduceUseCase introduceUseCase = new IntroduceUseCase(challengerRepository);
        IntroduceUseCase.Result result = introduceUseCase.execute(event.getAuthor().getIdLong(), args[0]);

        String message;

        if (result.getError() == null) {
            message = String.format("%s, You have been acknowledged as %s!", event.getAuthor().getAsMention(), result.getNewName());
        } else {
            message = switch (result.getError()) {
                case CHALLENGER_ALREADY_REGISTERED -> "I already know you...";
            };
        }

        channel.sendMessage(message).queue();
    }

    private void onChallengeRequested(MessageReceivedEvent event) {
        MessageChannel channel = event.getChannel();

        StartChallengeUseCase startChallengeUseCase = new StartChallengeUseCase(
                challengerRepository,
                challengeRepository,
                gameRulesProvider,
                (min, max) -> 3
        );

        StartChallengeUseCase.Result result = startChallengeUseCase.execute(event.getAuthor().getIdLong());

        String message;

        if (result.getError() == null) {
            message = String.format("%s, I offer you %d. Do you accept?", event.getAuthor().getAsMention(), 1);
        } else {
            message = switch (result.getError()) {
                case CHALLENGE_ALREADY_ACTIVE -> "You are not done yet!";
                case CHALLENGER_NOT_REGISTERED -> "A Challenge? Don't you think I deserve a proper introduction first?";
            };
        }

        channel.sendMessage(message).queue();
    }

    private void onChallengeAccepted(MessageReceivedEvent event) {

    }

    private static ChallengerRepository createChallengerRepositoryStub() {
        return new ChallengerRepository() {
            private final Map<Long, Challenger> data = new HashMap<>();

            @Override
            public Challenger findByDiscordId(long id) {
                return data.get(id);
            }

            @Override
            public void insert(long discordId, String inGameName) {
                data.put(discordId, new Challenger(String.valueOf(discordId), inGameName, discordId));
            }
        };
    }

    private static ChallengeRepository createChallengeRepositoryStub() {
        return new ChallengeRepository() {
            private final Map<String, Challenge> data = new HashMap<>();

            @Override
            public Challenge findByChallengerId(String id) {
                return data.get(id);
            }

            @Override
            public void insert(String challengerId, Map<Integer, Integer> availableGods, boolean isActive) {
                data.put(challengerId, new Challenge(availableGods, isActive));
            }

            @Override
            public void update(String challengerId, Challenge newChallenge) {
                data.put(challengerId, newChallenge);
            }
        };
    }

    private static GameRulesProvider createGameRulesProviderStub() {
        return new GameRulesProvider() {
            @Override
            public int getGodsCount() {
                return 40;
            }

            @Override
            public int getChallengeProposedGodsCount() {
                return 3;
            }

            @Override
            public int getBaseRerolls() {
                return 0;
            }
        };
    }
}
