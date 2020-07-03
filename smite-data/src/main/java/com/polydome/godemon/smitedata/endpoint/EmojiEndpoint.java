package com.polydome.godemon.smitedata.endpoint;

import com.polydome.godemon.smitedata.entity.HostedEmoji;
import io.reactivex.Single;

import java.util.List;

public interface EmojiEndpoint {
    Single<List<HostedEmoji>> fetchEmojisFromHost(long guildId);
}
