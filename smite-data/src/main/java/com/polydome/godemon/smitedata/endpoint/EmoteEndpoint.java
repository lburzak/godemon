package com.polydome.godemon.smitedata.endpoint;

import com.polydome.godemon.smitedata.entity.HostedEmote;
import io.reactivex.Single;

import java.util.List;

public interface EmoteEndpoint {
    Single<List<HostedEmote>> fetchEmotesFromHost(long guildId);
}
