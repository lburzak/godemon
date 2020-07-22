package com.polydome.godemon.discordbot.reaction;

import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public interface ActionListener {
    void onCreateChallenge(MessageReactionAddEvent event);
    void onAcceptChallenge(MessageReactionAddEvent event, int challengeId);
    void onJoinChallenge(MessageReactionAddEvent event, int challengeId);
}
