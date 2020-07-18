package com.polydome.godemon.discordbot.command.parser;

import org.springframework.stereotype.Service;

@Service
public class CommandLineParser {
    public CommandLine parseInput(String input, String prefix) {
        if (input.startsWith(prefix)) {
            String[] argv = input.substring(prefix.length() + 1).split(" ");

            int argsCount = argv.length - 1;
            String[] args = new String[argsCount];

            if (argsCount - 1 >= 0)
                System.arraycopy(argv, 1, args, 0, argsCount);

            return new CommandLine(argv[0], argsCount, args);
        } else {
            return null;
        }
    }
}
