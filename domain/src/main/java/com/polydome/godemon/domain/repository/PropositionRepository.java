package com.polydome.godemon.domain.repository;

import com.polydome.godemon.domain.entity.Proposition;

public interface PropositionRepository {
    Proposition findPropositionByChallengerId(long id);
    void createProposition(long challengerId, int[] gods, long messageId);
    void deleteProposition(long challengerId);
}
