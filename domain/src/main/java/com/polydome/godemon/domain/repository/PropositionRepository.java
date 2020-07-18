package com.polydome.godemon.domain.repository;

import com.polydome.godemon.domain.entity.Proposition;
import com.polydome.godemon.domain.repository.exception.CRUDException;

public interface PropositionRepository {
    Proposition findProposition(int challengeId, long challengerId);
    void createProposition(Proposition proposition) throws CRUDException;
    void deleteProposition(int challengeId, long challengerId);
}
