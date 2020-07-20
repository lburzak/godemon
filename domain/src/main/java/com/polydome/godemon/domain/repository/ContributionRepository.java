package com.polydome.godemon.domain.repository;

import com.polydome.godemon.domain.entity.Contribution;

import java.util.List;

public interface ContributionRepository {
    void insertAll(int challengeId, List<Contribution> contributions);
}
