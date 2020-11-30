package com.faa.chain.consensus.events;

import lombok.Builder;

@Builder(toBuilder = true)
public class BftProposalEvent {
    private int data;
}
