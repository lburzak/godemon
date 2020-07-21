package com.polydome.godemon.discordbotapp;

import com.polydome.godemon.discordbot.reaction.ActionArgStorage;
import com.polydome.godemon.discordbot.reaction.ActionCodeStorage;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class InMemoryActionCache implements ActionCodeStorage, ActionArgStorage {
    private final Map<Long, Integer> codes = new HashMap<>();
    private final Map<String, Integer> args = new HashMap<>();

    @Override
    public void setIntArg(long messageId, int index, int arg) {
        args.put(messageId + ":" + index, arg);
    }

    @Override
    public Integer getIntArg(long messageId, int index) throws IllegalStateException {
        Integer arg = args.get(messageId + ":" + index);
        if (arg == null) {
            throw new IllegalStateException(String.format("Argument %d not set", index));
        } else {
            return arg;
        }
    }

    @Override
    public void setCode(long messageId, int code) {
        codes.put(messageId, code);
    }

    @Override
    public Integer getCode(long messageId) {
        return codes.get(messageId);
    }

    @Override
    public void clearCode(long messageId) {
        codes.remove(messageId);
    }
}
