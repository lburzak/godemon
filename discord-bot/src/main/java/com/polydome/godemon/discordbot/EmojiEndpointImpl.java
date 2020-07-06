package com.polydome.godemon.discordbot;

import com.polydome.godemon.smitedata.endpoint.EmojiEndpoint;
import com.polydome.godemon.smitedata.entity.HostedEmoji;
import io.reactivex.Single;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EmojiEndpointImpl implements EmojiEndpoint {
    private final JDA jda;

    public EmojiEndpointImpl(JDA jda) {
        this.jda = jda;
    }

    private HostedEmoji serializeEmote(Emote emote) {
        return new HostedEmoji(
                emote.getIdLong(),
                emote.getName()
        );
    }

    @Override
    public Single<List<HostedEmoji>> fetchEmojisFromHost(long guildId) {
        return Single.create(emitter -> {
            emitter.onSuccess(
                Objects.requireNonNull(jda.getGuildById(guildId))
                        .getEmotes().stream()
                        .map(this::serializeEmote)
                        .collect(Collectors.toList())
            );
        });
    }
}
