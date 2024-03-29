package com.polydome.godemon.domain.usecase;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.entity.Proposition;
import com.polydome.godemon.domain.exception.ActionForbiddenException;
import com.polydome.godemon.domain.exception.AuthenticationException;
import com.polydome.godemon.domain.exception.NoSuchChallengeException;
import com.polydome.godemon.domain.model.ChallengeProposition;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import com.polydome.godemon.domain.repository.ChallengerRepository;
import com.polydome.godemon.domain.repository.ChampionRepository;
import com.polydome.godemon.domain.repository.PropositionRepository;
import com.polydome.godemon.domain.repository.exception.NoSuchEntityException;
import com.polydome.godemon.domain.service.ChallengeService;
import com.polydome.godemon.domain.service.GameRulesProvider;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JoinChallengeUseCase {
    private final ChallengeService challengeService;
    private final ChallengeRepository challengeRepository;
    private final ChallengerRepository challengerRepository;
    private final ChampionRepository championRepository;
    private final GameRulesProvider gameRulesProvider;
    private final PropositionRepository propositionRepository;

    public ChallengeProposition withChallengeId(long discordId, int challengeId) throws AuthenticationException, ActionForbiddenException {
        Challenger challenger;

        try {
            challenger = challengerRepository.findChallengerById(discordId);
        } catch (NoSuchEntityException e) {
            throw new AuthenticationException("Challenger not registered");
        }

        // TODO: Check if challenge exists

        Challenge challenge = challengeRepository.findChallenge(challengeId);
        if (challenge == null) {
            throw new NoSuchChallengeException(challengeId);
        }

        if (challenge.getParticipants().stream().anyMatch(participant -> participant.getId() == challenger.getId()))
            throw new ActionForbiddenException(String.format("Challenger is already a participant of challenge [%d]", challenge.getId()));

        try {
            Proposition existingProposition = propositionRepository.findProposition(challengeId, discordId);

            return new ChallengeProposition(existingProposition.getGods());
        } catch (NoSuchEntityException e) {
            int[] gods = championRepository.getRandomIds(gameRulesProvider.getChallengeProposedGodsCount());

            propositionRepository.createProposition(
                    Proposition.builder()
                            .challengeId(challengeId)
                            .gods(gods)
                            .requesterId(discordId)
                            .build()
            );

            return new ChallengeProposition(gods);
        }
    }
}
