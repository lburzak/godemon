package com.polydome.godemon.smitedata.implementation;

import com.polydome.godemon.discordbot.view.service.GodData;
import com.polydome.godemon.discordbot.view.service.GodsDataProvider;
import com.polydome.godemon.domain.repository.ChampionRepository;
import com.polydome.godemon.smitedata.entity.SmiteChampion;
import com.polydome.godemon.smitedata.repository.SmiteChampionRepository;

public class SmiteGodsDataProvider implements GodsDataProvider, ChampionRepository {
    private final SmiteChampionRepository smiteChampionRepository;

    public SmiteGodsDataProvider(SmiteChampionRepository smiteChampionRepository) {
        this.smiteChampionRepository = smiteChampionRepository;
    }

    private GodData serializeSmiteChampion(SmiteChampion champion) {
        return new GodData(
                champion.id,
                champion.emoteId,
                champion.displayName
        );
    }

    @Override
    public GodData findById(int id) {
        return smiteChampionRepository.findById(id)
                .map(this::serializeSmiteChampion)
                .blockingGet();
    }

    @Override
    public GodData findByEmote(String emoteId) {
        return smiteChampionRepository.findByEmote(emoteId)
                .map(this::serializeSmiteChampion)
                .blockingGet();
    }

    @Override
    public int[] getRandomIds(int count) {
        return smiteChampionRepository.getRandomIds(count)
                .blockingGet();
    }
}
