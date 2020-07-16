package com.polydome.godemon.domain.usecase;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.entity.Proposition;
import com.polydome.godemon.domain.exception.AuthenticationException;
import com.polydome.godemon.domain.exception.NoSuchChallengeException;
import com.polydome.godemon.domain.exception.NoSuchPropositionException;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import com.polydome.godemon.domain.repository.ChallengerRepository;
import com.polydome.godemon.domain.repository.PropositionRepository;
import lombok.AllArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@AllArgsConstructor
public class AcceptChallengeUseCase {
    private final ChallengerRepository challengerRepository;
    private final ChallengeRepository challengeRepository;
    private final PropositionRepository propositionRepository;

    public int execute(long discordId, long messageId, int godIdChoice) {
        Proposition proposition = propositionRepository.findProposition(messageId);
        if (proposition == null)
            throw new NoSuchPropositionException(String.format("Proposition not found [%d]", messageId));

        Challenger challenger = challengerRepository.findByDiscordId(discordId);
        if (challenger == null)
            throw new AuthenticationException(String.format("Challenger [%d] not registered", discordId));

        Challenge challenge = challengeRepository.findChallenge(proposition.getChallengeId());
        if (challenge == null)
            throw new NoSuchChallengeException(proposition.getChallengeId());

        GodPool godPool = new GodPool(challenge.getAvailableGods());
        godPool.grantOne(godIdChoice);

        List<Challenger> participants = new LinkedList<>(challenge.getParticipants());
        participants.add(challenger);

        challengeRepository.updateChallenge(
                challenge.toBuilder()
                    .availableGods(godPool.toMap())
                    .participants(participants)
                    .build()
        );

        propositionRepository.deleteProposition(messageId);

        return godIdChoice;
    }
}
