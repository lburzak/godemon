package com.polydome.godemon.smiteapi.json;

import com.polydome.godemon.smiteapi.Queue;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

public class QueueAdapter {
    @FromJson
    Queue fromJson(int matchQueueId) {
        return Queue.fromId(matchQueueId);
    }

    @ToJson
    int toJson(Queue queue) {
        return queue.getId();
    }
}
