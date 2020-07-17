package com.polydome.godemon.domain.usecase;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.ChallengeStage;
import com.polydome.godemon.domain.exception.ActionForbiddenException;
import com.polydome.godemon.domain.exception.AuthenticationException;
import com.polydome.godemon.domain.exception.NoSuchChallengeException;
import com.polydome.godemon.domain.model.ChallengeStatus;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import com.polydome.godemon.domain.repository.ChallengerRepository;
import com.polydome.godemon.domain.repository.exception.NoSuchEntityException;
import com.polydome.godemon.domain.service.ChallengeService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GetChallengeStatusUseCase {
    private final ChallengerRepository challengerRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeService challengeService;

    public ChallengeStatus execute(long challengerId, int challengeId) throws ActionForbiddenException, AuthenticationException, NoSuchChallengeException {
        try {
            challengerRepository.findChallengerById(challengerId);
        } catch (NoSuchEntityException e) {
            throw new AuthenticationException(String.format("Challenger[%d] not registered", challengerId));
        }

        Challenge challenge;

        try {
            challenge = challengeRepository.findChallenge(challengeId);
        } catch (NoSuchEntityException e) {
            throw new NoSuchChallengeException(challengeId);
        }

        if (challenge.getParticipants().stream().noneMatch(challenger -> challenger.getId() == challengerId))
            throw new ActionForbiddenException(String.format("Challenger[%d] does not participate in Challenge[%d]", challengerId, challengeId));

        challengeService.synchronizeChallenge(challengeId);
        challenge = challengeRepository.findChallenge(challengeId);

        return ChallengeStatus.builder()
                .ended(challenge.getStatus() == ChallengeStage.FAILED)
                .godToUsesLeft(challenge.getAvailableGods())
                .godsLeftCount(challenge.getAvailableGods().size())
                .wins(0)
                .loses(0)
                .build();
    }

}
