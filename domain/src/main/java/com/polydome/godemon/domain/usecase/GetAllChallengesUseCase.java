package com.polydome.godemon.domain.usecase;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.exception.AuthenticationException;
import com.polydome.godemon.domain.model.ChallengeBrief;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import com.polydome.godemon.domain.repository.ChallengerRepository;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class GetAllChallengesUseCase {
    private final ChallengerRepository challengerRepository;
    private final ChallengeRepository challengeRepository;

    public List<ChallengeBrief> execute(long discordId) throws AuthenticationException {
        if (!challengerRepository.existsChallenger(discordId)) {
            throw new AuthenticationException("Challenger not registered");
        }

        List<Challenge> challenges = challengeRepository.findAllChallenges();

        return challenges.stream().map(ChallengeBrief::fromChallenge).collect(Collectors.toList());
    }
}
