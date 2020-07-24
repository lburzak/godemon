package com.polydome.godemon.discordbot.listener;

import com.polydome.godemon.discordbot.command.DiscordCommandHandler;
import com.polydome.godemon.discordbot.command.parser.CommandLine;
import com.polydome.godemon.discordbot.command.parser.CommandLineParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

@Service
public class CommandListener extends ListenerAdapter {
    private final CommandLineParser parser;
    private final DiscordCommandHandler commandHandler;
    private final @Named("CommandPrefix") String prefix;

    @Inject
    public CommandListener(CommandLineParser parser, DiscordCommandHandler commandHandler, @Named("CommandPrefix") String prefix) {
        this.parser = parser;
        this.commandHandler = commandHandler;
        this.prefix = prefix;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getAuthor().isBot())
            return;

        CommandLine cmd = parser.parseInput(event.getMessage().getContentRaw(), prefix);

        if (cmd != null) {
            commandHandler.onCommand(cmd, event);
        }
    }
}
