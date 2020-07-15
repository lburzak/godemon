package com.polydome.godemon.domain.exception;

import com.polydome.godemon.domain.entity.Challenge;

import java.util.MissingResourceException;

public class NoSuchChallengeException extends MissingResourceException {
    public NoSuchChallengeException(int challengeId) {
        super("Challenge not found", Challenge.class.getName(), String.valueOf(challengeId));
    }
}
