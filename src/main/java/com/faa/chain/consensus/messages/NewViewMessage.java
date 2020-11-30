/**
 * Copyright (c) 2019 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.faa.chain.consensus.messages;

import com.faa.chain.consensus.Proof;
import com.faa.chain.net.Message;
import com.faa.chain.net.MessageCode;

public class NewViewMessage extends Message {

    private final Proof proof;

    public NewViewMessage(Proof proof) {
        super(MessageCode.BFT_NEW_VIEW, null);

        this.proof = proof;

        // TODO: consider wrapping by simple codec
        this.body = proof.toBytes();
    }

    public NewViewMessage(byte[] body) {
        super(MessageCode.BFT_NEW_VIEW, null);

        this.proof = Proof.fromBytes(body);

        this.body = body;
    }

    public Proof getProof() {
        return proof;
    }

    public int getHeight() {
        return proof.getHeight();
    }

    public int getView() {
        return proof.getView();
    }

    @Override
    public String toString() {
        return "BFTNewViewMessage [proof=" + proof + "]";
    }
}