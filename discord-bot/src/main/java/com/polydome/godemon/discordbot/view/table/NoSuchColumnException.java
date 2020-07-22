package com.polydome.godemon.discordbot.view.table;

public class NoSuchColumnException extends IllegalArgumentException {
    public NoSuchColumnException(String columnName) {
        super(String.format("Column %s does not exist", columnName));
    }
}
