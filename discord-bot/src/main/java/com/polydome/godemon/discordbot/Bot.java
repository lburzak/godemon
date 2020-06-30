package com.polydome.godemon.discordbot;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import com.polydome.godemon.domain.repository.ChallengerRepository;
import com.polydome.godemon.domain.usecase.ChallengeNotFoundException;
import com.polydome.godemon.domain.usecase.ChallengerNotFoundException;
import com.polydome.godemon.domain.usecase.GetChallengeStatusUseCase;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;

public class Bot extends ListenerAdapter {
    public static void main(String[] args) throws LoginException {
        if (args.length < 1) {
            System.out.println("You have to provide a token as first argument!");
            System.exit(1);
        }

        JDABuilder.createLight(args[0], GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES)
                .addEventListeners(new Bot())
                .build();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        if (msg.getContentRaw().equals(";godemon challenge")) {
            MessageChannel channel = event.getChannel();

            String message = "CHALLENGE";

            ChallengerRepository challengerRepository = id -> {
                throw new ChallengerNotFoundException();
            };

            ChallengeRepository challengeRepository = Id -> {
                throw new ChallengeNotFoundException();
            };

            GetChallengeStatusUseCase getChallengeStatusUseCase = new GetChallengeStatusUseCase(challengerRepository, challengeRepository);
            GetChallengeStatusUseCase.Result result = getChallengeStatusUseCase.execute(event.getAuthor().getIdLong());

            message = switch (result.error) {
                case CHALLENGER_NOT_REGISTERED -> "Eh? never heard of him.";
                case CHALLENGE_NOT_ACTIVE -> "You have no active challenge. Do you want to begin?";
            };

            channel.sendMessage(message).queue();
        }
    }
}
