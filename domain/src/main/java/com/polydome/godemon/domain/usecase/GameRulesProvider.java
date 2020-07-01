package com.polydome.godemon.domain.usecase;

public interface GameRulesProvider {
    int getGodsCount();
    int getChallengeProposedGodsCount();
    int getBaseRerolls();
}
