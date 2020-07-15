package com.polydome.godemon.data.dao;

import com.polydome.godemon.domain.entity.Proposition;
import com.polydome.godemon.domain.repository.PropositionRepository;

public class PropositionDAO implements PropositionRepository {
    @Override
    public Proposition findProposition(long messageId) {
        return null;
    }

    @Override
    public void createProposition(int challengeId, int[] gods, long messageId) {

    }

    @Override
    public void deleteProposition(long messageId) {

    }
}
