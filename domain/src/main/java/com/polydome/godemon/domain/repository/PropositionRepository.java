package com.polydome.godemon.domain.repository;

import com.polydome.godemon.domain.entity.Proposition;

public interface PropositionRepository {
    Proposition findProposition(long messageId);
    void createProposition(int challengeId, int[] gods, long messageId);
    void deleteProposition(long messageId);
}
