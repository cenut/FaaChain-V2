package com.faa.chain.consensus.events;

import lombok.Builder;

@Builder(toBuilder = true)
public class BftVoteEvent {
    private int data;
}
