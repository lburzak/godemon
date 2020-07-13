package com.polydome.godemon.smiteapi;

import com.squareup.moshi.Json;
import lombok.Data;

@Data
public class GodDefinition {
    @Json(name = "id") public final int id;
    @Json(name = "Name") public final String name;
}
