package com.polydome.godemon.smiteapi.json;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class InstantAdapter {
    private final DateTimeFormatter formatter = DateTimeFormatter
            .ofPattern("M/d/uuuu h:mm:ss a")
            .withZone( ZoneId.of("UTC"));

    @FromJson
    Instant fromJson(String timestamp) {
        return Instant.from(formatter.parse(timestamp));
    }

    @ToJson
    String toJson(Instant timestamp){
        return timestamp.toString();
    }
}
