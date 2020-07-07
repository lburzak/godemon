package com.polydome.godemon.smitedata.endpoint;

import lombok.Getter;

public class EmoteHostNotAvailableException extends Exception {
    @Getter
    private final long guildId;

    public EmoteHostNotAvailableException(long guildId) {
        this.guildId = guildId;
    }
}
