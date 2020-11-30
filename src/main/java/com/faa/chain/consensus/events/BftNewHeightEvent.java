package com.faa.chain.consensus.events;

import lombok.Builder;

@Builder(toBuilder = true)
public class BftNewHeightEvent {
    private long height;

    public long getHeight() {
        return height;
    }
}
