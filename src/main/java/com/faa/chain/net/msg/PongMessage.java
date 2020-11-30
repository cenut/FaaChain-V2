package com.faa.chain.net.msg;

import com.faa.chain.net.Message;
import com.faa.chain.net.MessageCode;
import com.faa.chain.net.SimpleDecoder;
import com.faa.chain.net.SimpleEncoder;
import com.faa.chain.utils.TimeUtil;

public class PongMessage extends Message {

    private final long timestamp;

    /**
     * Create a PONG message.
     */
    public PongMessage() {
        super(MessageCode.PONG, null);

        this.timestamp = TimeUtil.currentTimeMillis();

        SimpleEncoder enc = new SimpleEncoder();
        enc.writeLong(timestamp);
        this.body = enc.toBytes();
    }

    /**
     * Parse a PONG message from byte array.
     *
     * @param body
     */
    public PongMessage(byte[] body) {
        super(MessageCode.PONG, null);

        SimpleDecoder dec = new SimpleDecoder(body);
        this.timestamp = dec.readLong();

        this.body = body;
    }

    @Override
    public String toString() {
        return "PongMessage [timestamp=" + timestamp + "]";
    }
}
