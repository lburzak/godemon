package com.polydome.godemon.domain.usecase;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.model.ChallengeBrief;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class GetAvailableChallengesUseCase {
    private final ChallengeRepository challengeRepository;

    List<ChallengeBrief> withChallengerId(long id) {
        List<Challenge> challenges = challengeRepository.findChallengesByParticipant(id);

        return challenges.stream()
                .map(this::challengeToBrief)
               .collect(Collectors.toList());
    }

    private ChallengeBrief challengeToBrief(Challenge challenge) {
        return ChallengeBrief.builder()
                .id(challenge.getId())
                .lastUpdate(challenge.getLastUpdate())
                .build();
    }
}
