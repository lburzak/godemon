package com.polydome.godemon.domain.repository;

import com.polydome.godemon.domain.entity.Proposition;

public interface PropositionRepository {
    Proposition findByChallengerId(String id);
    void insert(String challengerId, int[] gods, int rerolls, long messageId);
}
