package com.faa.chain.net.msg;

import com.faa.chain.net.Message;
import com.faa.chain.net.MessageCode;
import com.faa.chain.net.SimpleDecoder;
import com.faa.chain.net.SimpleEncoder;

public class InitMessage extends Message {
    public static final int SECRET_LENGTH = 32;

    private final byte[] secret;
    private final long timestamp;

    public InitMessage(byte[] secret, long timestamp) {
        super(MessageCode.HANDSHAKE_INIT, null);

        this.secret = secret;
        this.timestamp = timestamp;

        SimpleEncoder enc = new SimpleEncoder();
        enc.writeBytes(secret);
        enc.writeLong(timestamp);

        this.body = enc.toBytes();
    }

    public InitMessage(byte[] body) {
        super(MessageCode.HANDSHAKE_INIT, null);

        SimpleDecoder dec = new SimpleDecoder(body);
        this.secret = dec.readBytes();
        this.timestamp = dec.readLong();

        this.body = body;
    }

    public boolean validate() {
        return secret != null && secret.length == SECRET_LENGTH
                && timestamp > 0;
    }

    public byte[] getSecret() {
        return secret;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "InitMessage{" +
                "secret=" + secret +
                ", timestamp=" + timestamp +
                '}';
    }
}
