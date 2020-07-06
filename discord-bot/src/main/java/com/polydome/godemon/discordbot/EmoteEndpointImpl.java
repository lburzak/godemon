package com.polydome.godemon.discordbot;

import com.polydome.godemon.smitedata.endpoint.EmoteEndpoint;
import com.polydome.godemon.smitedata.entity.HostedEmote;
import io.reactivex.Single;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EmoteEndpointImpl implements EmoteEndpoint {
    private final JDA jda;

    public EmoteEndpointImpl(JDA jda) {
        this.jda = jda;
    }

    private HostedEmote serializeEmote(Emote emote) {
        return new HostedEmote(
                emote.getIdLong(),
                emote.getName()
        );
    }

    @Override
    public Single<List<HostedEmote>> fetchEmotesFromHost(long guildId) {
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
