package com.polydome.godemon.discordbot.emote;

import net.dv8tion.jda.api.entities.MessageReaction;
import org.springframework.stereotype.Service;

@Service
public class EmoteManager {
    private final String DIGIT_EMOTE_SUFFIX = "️⃣";

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
}
