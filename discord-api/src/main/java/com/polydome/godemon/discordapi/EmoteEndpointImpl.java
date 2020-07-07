package com.polydome.godemon.discordapi;

import com.polydome.godemon.smitedata.endpoint.EmoteEndpoint;
import com.polydome.godemon.smitedata.endpoint.EmoteHostNotAvailableException;
import com.polydome.godemon.smitedata.entity.HostedEmote;
import io.reactivex.Single;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;

import java.util.List;
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
            if (jda.getStatus() != JDA.Status.CONNECTED)
                jda.awaitReady();

            Guild guild = jda.getGuildById(guildId);

            if (guild == null) {
                emitter.onError(new EmoteHostNotAvailableException(guildId));
            } else {
                emitter.onSuccess(
                        guild.getEmotes().stream()
                                .map(this::serializeEmote)
                                .collect(Collectors.toList())
                );
            }
        });
    }
}
