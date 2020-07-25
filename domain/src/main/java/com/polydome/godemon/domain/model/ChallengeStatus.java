package com.polydome.godemon.domain.model;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.ChallengeStage;
import com.polydome.godemon.domain.entity.Challenger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class ChallengeStatus {
    private final int wins;
    private final int loses;
    private final int godsLeftCount;
    private final Map<Integer, Integer> godToUsesLeft;
    private final boolean ended;
    private final List<String> participants;
    private final Instant createdAt;

    public static ChallengeStatus fromChallenge(final Challenge challenge, final int wins, final int loses) {
        return ChallengeStatus.builder()
                .ended(challenge.getStatus() == ChallengeStage.FAILED)
                .godToUsesLeft(challenge.getAvailableGods())
                .godsLeftCount(challenge.getAvailableGods().size())
                .participants(challenge.getParticipants().stream().map(Challenger::getInGameName).collect(Collectors.toList()))
                .createdAt(challenge.getCreatedAt())
                .wins(wins)
                .loses(loses)
                .build();
    }
}
