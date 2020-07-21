package com.polydome.godemon.discordbot.view.action;

import com.polydome.godemon.discordbot.reaction.NoSuchActionException;

public enum Action {
    CREATE_CHALLENGE,
    JOIN_CHALLENGE;

    private final static Action[] actions = Action.values();

    public static int getCode(Action action) {
        short i = 0;
        for (final Action knownAction : actions) {
            if (knownAction == action)
                return i;

            i++;
        }

        return -1;
    }

    public static Action getAction(int code) throws NoSuchActionException {
        try {
            return actions[code];
        } catch (IndexOutOfBoundsException e) {
            throw new NoSuchActionException(code);
        }
    }
}
