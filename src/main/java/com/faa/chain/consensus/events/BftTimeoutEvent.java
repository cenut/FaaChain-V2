package com.faa.chain.consensus.events;

import lombok.Builder;

@Builder(toBuilder = true)
public class BftTimeoutEvent {

    private int data;

}
