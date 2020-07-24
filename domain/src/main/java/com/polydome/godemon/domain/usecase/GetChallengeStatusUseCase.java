package com.polydome.godemon.domain.usecase;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.ChallengeStage;
import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.entity.Contribution;
import com.polydome.godemon.domain.exception.ActionForbiddenException;
import com.polydome.godemon.domain.exception.AuthenticationException;
import com.polydome.godemon.domain.exception.NoSuchChallengeException;
import com.polydome.godemon.domain.model.ChallengeStatus;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import com.polydome.godemon.domain.repository.ChallengerRepository;
import com.polydome.godemon.domain.repository.ContributionRepository;
import com.polydome.godemon.domain.repository.exception.NoSuchEntityException;
import com.polydome.godemon.domain.service.ChallengeService;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class GetChallengeStatusUseCase {
    private final ChallengerRepository challengerRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeService challengeService;
    private final ContributionRepository contributionRepository;

    public ChallengeStatus execute(long challengerId, int challengeId, boolean doUpdate) throws ActionForbiddenException, AuthenticationException, NoSuchChallengeException {
        try {
            challengerRepository.findChallengerById(challengerId);
        } catch (NoSuchEntityException e) {
            throw new AuthenticationException(String.format("Challenger[%d] not registered", challengerId));
        }

        Challenge challenge;

        if (doUpdate)
            challengeService.synchronizeChallenge(challengeId);

        try {
            challenge = challengeRepository.findChallenge(challengeId);
        } catch (NoSuchEntityException e) {
            throw new NoSuchChallengeException(challengeId);
        }

        List<Contribution> contributions = contributionRepository.findContributionsByChallenge(challengeId);

        long matchesCount = contributions.stream()
                .map(Contribution::getMatchId)
                .distinct()
                .count();

        long wins = contributions.stream()
                .filter(Contribution::isWin)
                .map(Contribution::getMatchId)
                .distinct()
                .count();

        return ChallengeStatus.builder()
                .ended(challenge.getStatus() == ChallengeStage.FAILED)
                .godToUsesLeft(challenge.getAvailableGods())
                .godsLeftCount(challenge.getAvailableGods().size())
                .participants(challenge.getParticipants().stream().map(Challenger::getInGameName).collect(Collectors.toList()))
                .wins((int) wins)
                .loses((int) (matchesCount - wins))
                .build();
    }

}
