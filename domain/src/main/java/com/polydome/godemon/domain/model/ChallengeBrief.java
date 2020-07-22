package com.polydome.godemon.domain.model;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.GameMode;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ChallengeBrief {
    private final int id;
    private final GameMode gameMode;
    private final Instant lastUpdate;

    public static ChallengeBrief fromChallenge(Challenge challenge) {
        return ChallengeBrief.builder()
                .id(challenge.getId())
                .gameMode(challenge.getGameMode())
                .lastUpdate(challenge.getLastUpdate())
                .build();
    }
}
