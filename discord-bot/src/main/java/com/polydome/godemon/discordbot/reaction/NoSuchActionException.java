package com.polydome.godemon.discordbot.reaction;

public class NoSuchActionException extends Exception {
    public NoSuchActionException(int code) {
        super(String.format("Unknown action code: %d", code));
    }
}
