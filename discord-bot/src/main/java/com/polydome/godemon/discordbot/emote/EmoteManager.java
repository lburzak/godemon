package com.polydome.godemon.discordbot.emote;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.MessageReaction;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EmoteManager {
    private final String DIGIT_EMOTE_SUFFIX = "️⃣";
    private final JDA jda;
    private final EmoteStore emoteStore;

    public EmoteManager(JDA jda, EmoteStore emoteStore) {
        this.jda = jda;
        this.emoteStore = emoteStore;
    }

    public String fromDigit(final byte digit) {
        if (digit > 9 || digit < 0)
            throw new IllegalArgumentException("Digit is expected to be within <0,9> range");

        return digit + DIGIT_EMOTE_SUFFIX;
    }

    public short toDigit(final MessageReaction.ReactionEmote emote) {
        if (!emote.isEmoji()) {
            throw new IllegalArgumentException("Emote is expected to be an emoji");
        }

        String emoji = emote.getEmoji();

        if (emoji.length() == 3 && emoji.charAt(0) >= '0' && emoji.charAt(0) <= '9' && emoji.endsWith(DIGIT_EMOTE_SUFFIX) ) {
            return (short) (emoji.charAt(0) - '0');
        } else {
            throw new IllegalArgumentException("Emoji is not digit-like");
        }
    }

    public Emote findByName(String name) {
        if (emoteStore.has(name)) {
            Emote emote = jda.getEmoteById(emoteStore.findId(name));
            if (emote == null)
                throw new MissingEmoteException("Emote not present in cache", name);
            return emote;
        } else {
            throw new MissingEmoteException("Unknown emote", name);
        }
    }
}
