package com.polydome.godemon.domain.service.matchdetails;

import lombok.Data;

@Data
public class MatchDetails {
    private final int id;
    private final byte participantsCount;
    private final PlayerRecord[] players;
}
