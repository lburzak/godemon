package com.polydome.godemon.discordbot.command.parser;

public class CommandLine {
    public final String command;
    public final int argsCount;
    public final String[] args;

    public CommandLine(String command, int argsCount, String[] args) {
        this.command = command;
        this.argsCount = argsCount;
        this.args = args;
    }
}
