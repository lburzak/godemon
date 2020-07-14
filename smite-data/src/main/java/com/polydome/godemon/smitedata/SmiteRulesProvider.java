package com.polydome.godemon.smitedata;

import com.polydome.godemon.domain.service.GameRulesProvider;
import com.polydome.godemon.smitedata.repository.GodsRepository;

public class SmiteRulesProvider implements GameRulesProvider {
    private final GodsRepository godsRepository;

    public SmiteRulesProvider(GodsRepository godsRepository) {
        this.godsRepository = godsRepository;
    }

    @Override
    public int getGodsCount() {
        return godsRepository.countAll().blockingGet();
    }

    @Override
    public int getChallengeProposedGodsCount() {
        return 3;
    }

    @Override
    public int getBaseRerolls() {
        return 0;
    }
}
