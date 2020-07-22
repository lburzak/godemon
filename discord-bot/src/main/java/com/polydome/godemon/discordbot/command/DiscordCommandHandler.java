package com.polydome.godemon.discordbot.command;

import com.polydome.godemon.discordbot.command.parser.CommandLine;
import com.polydome.godemon.discordbot.view.DiscordChallengeView;
import com.polydome.godemon.presentation.contract.ChallengeContract;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Service;

@Service
public class DiscordCommandHandler {
    private final ChallengeContract.Presenter challengePresenter;
    private final DiscordChallengeView.Factory challengeViewFactory;

    public DiscordCommandHandler(ChallengeContract.Presenter challengePresenter, DiscordChallengeView.Factory challengeViewFactory) {
        this.challengePresenter = challengePresenter;
        this.challengeViewFactory = challengeViewFactory;
    }

    public void onCommand(CommandLine commandLine, MessageReceivedEvent event) {
        long authorId = event.getAuthor().getIdLong();

        switch (commandLine.command) {
            case "challenge": {
                if (commandLine.argsCount == 1) {
                    if (commandLine.args[0].equals("new")) {
                        challengePresenter.onCreateChallenge(
                                challengeViewFactory.create(event),
                                event.getAuthor().getIdLong()
                        );
                    } else {
                        int challengeId = 1;
                        try {
                            challengeId = Integer.parseInt(commandLine.args[0]);
                        } catch (NumberFormatException e) {
                            event.getChannel().sendMessage("Invalid argument").queue();
                        }

                        challengePresenter.onShowChallengeStatus(
                                challengeViewFactory.create(event),
                                challengeId,
                                authorId
                        );
                    }
                } else {
                    challengePresenter.onShowChallengesList(
                            challengeViewFactory.create(event),
                            authorId
                    );
                }

                break;
            }
            case "me":
                challengePresenter.onRegister(
                        challengeViewFactory.create(event),
                        authorId,
                        commandLine.args[0]
                );
                break;
            case "join":
                challengePresenter.onJoinChallenge(
                    challengeViewFactory.create(event),
                    Integer.parseInt(commandLine.args[0]),
                    authorId
                );
                break;
            case "lobby":
                challengePresenter.onLobbyRequest(
                        challengeViewFactory.create(event),
                        event.getAuthor().getIdLong()
                );
                break;
        }
    }
}
