/**
 * Copyright (c) 2019 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.faa.chain.consensus.messages;

import com.faa.chain.consensus.Vote;
import com.faa.chain.net.Message;
import com.faa.chain.net.MessageCode;

public class VoteMessage extends Message {

    private final Vote vote;

    public VoteMessage(Vote vote) {
        super(MessageCode.BFT_VOTE, null);

        this.vote = vote;

        // TODO: consider wrapping by simple codec
        this.body = vote.toBytes();
    }

    public VoteMessage(byte[] body) {
        super(MessageCode.BFT_VOTE, null);

        this.vote = Vote.fromBytes(body);

        this.body = body;
    }

    public Vote getVote() {
        return vote;
    }

    @Override
    public String toString() {
        return "BFTVoteMessage: " + vote;
    }
}
