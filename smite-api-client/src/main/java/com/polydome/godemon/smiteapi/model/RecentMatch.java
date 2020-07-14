package com.polydome.godemon.smiteapi.model;

import com.squareup.moshi.Json;
import lombok.Data;

import java.time.Instant;

@Data
public class RecentMatch {
    @Json(name = "Match") private final int id;
    @Json(name = "Match_Time") private final Instant date;
    @Json(name = "Match_Queue_Id") private final Queue queue;
}
