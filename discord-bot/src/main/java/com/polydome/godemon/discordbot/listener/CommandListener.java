package com.polydome.godemon.discordbot.listener;

import com.polydome.godemon.discordbot.command.DiscordCommandHandler;
import com.polydome.godemon.discordbot.command.parser.CommandLine;
import com.polydome.godemon.discordbot.command.parser.CommandLineParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

@Service
public class CommandListener extends ListenerAdapter {
    private final CommandLineParser parser;
    private final DiscordCommandHandler commandHandler;

    public CommandListener(CommandLineParser parser, DiscordCommandHandler commandHandler) {
        this.parser = parser;
        this.commandHandler = commandHandler;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getAuthor().isBot())
            return;

        CommandLine cmd = parser.parseInput(event.getMessage().getContentRaw(), ";godemon");

        if (cmd != null) {
            commandHandler.onCommand(cmd, event);
        }
    }
}
