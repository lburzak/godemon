package com.polydome.godemon.smiteapi.model;

import com.squareup.moshi.Json;
import lombok.Data;

@Data
public class MatchParticipantStats {
    public enum WinStatus {
        Winner,
        Loser
    }

    @Json(name = "playerId") public final int playerId;
    @Json(name = "hz_player_name") public final String playerName;
    @Json(name = "Kills_Player") public final byte kills;
    @Json(name = "Deaths") public final byte deaths;
    @Json(name = "GodId") public final int godId;
    @Json(name = "Win_Status") public final WinStatus winStatus;
    @Json(name = "Match") public final int matchId;
}
