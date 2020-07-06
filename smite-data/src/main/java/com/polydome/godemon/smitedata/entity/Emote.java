package com.polydome.godemon.smitedata.entity;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class Emote {
    public final int godId;
    public final String displayId;
    public final long hostedId;
    public final int hostId;
}
