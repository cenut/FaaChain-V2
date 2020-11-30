package com.faa.chain.net.msg;

import com.faa.chain.net.Message;
import com.faa.chain.net.MessageCode;
import com.faa.chain.net.SimpleDecoder;
import com.faa.chain.net.SimpleEncoder;
import com.faa.chain.utils.TimeUtil;

public class PingMessage extends Message {
    private final long timestamp;

    /**
     * Create a PING message.
     *
     */
    public PingMessage() {
        super(MessageCode.PING, PongMessage.class);

        this.timestamp = TimeUtil.currentTimeMillis();

        SimpleEncoder enc = new SimpleEncoder();
        enc.writeLong(timestamp);
        this.body = enc.toBytes();
    }

    /**
     * Parse a PING message from byte array.
     *
     * @param body
     */
    public PingMessage(byte[] body) {
        super(MessageCode.PING, PongMessage.class);

        SimpleDecoder dec = new SimpleDecoder(body);
        this.timestamp = dec.readLong();

        this.body = body;
    }

    @Override
    public String toString() {
        return "PingMessage [timestamp=" + timestamp + "]";
    }
}
