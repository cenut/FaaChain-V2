/**
 * Copyright (c) 2019 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.faa.chain.consensus.messages;

import com.faa.chain.net.Message;
import com.faa.chain.net.MessageCode;
import com.faa.chain.net.SimpleDecoder;
import com.faa.chain.net.SimpleEncoder;

public class NewHeightMessage extends Message {

    private final int height;

    public NewHeightMessage(int height) {
        super(MessageCode.BFT_NEW_HEIGHT, null);
        this.height = height;

        SimpleEncoder enc = new SimpleEncoder();
        enc.writeInt(height);
        this.body = enc.toBytes();
    }

    public NewHeightMessage(byte[] body) {
        super(MessageCode.BFT_NEW_HEIGHT, null);

        SimpleDecoder dec = new SimpleDecoder(body);
        this.height = dec.readInt();

        this.body = body;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return "BFTNewHeightMessage [height=" + height + "]";
    }
}
