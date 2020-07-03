package com.polydome.godemon.smiteapi;

import com.squareup.moshi.Json;
import lombok.Data;

@Data
class CreateSessionResponse {
    @Json(name = "ret_msg") public final String message;
    @Json(name = "session_id") public final String sessionId;
}
