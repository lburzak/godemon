package com.polydome.godemon.discordbot.emote;

import java.util.MissingResourceException;

public class MissingEmoteException extends MissingResourceException {
    public MissingEmoteException(String details, String key) {
        super(details, "Emote", key);
    }
}
