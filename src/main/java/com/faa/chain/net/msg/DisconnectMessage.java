package com.faa.chain.net.msg;

import com.faa.chain.net.*;

public class DisconnectMessage extends Message {

    private final ReasonCode reason;

    /**
     * Create a DISCONNECT message.
     *
     * @param reason
     */
    public DisconnectMessage(ReasonCode reason) {
        super(MessageCode.DISCONNECT, null);

        this.reason = reason;

        SimpleEncoder enc = new SimpleEncoder();
        enc.writeByte(reason.toByte());
        this.body = enc.toBytes();
    }

    /**
     * Parse a DISCONNECT message from byte array.
     *
     * @param body
     */
    public DisconnectMessage(byte[] body) {
        super(MessageCode.DISCONNECT, null);

        SimpleDecoder dec = new SimpleDecoder(body);
        this.reason = ReasonCode.of(dec.readByte());

        this.body = body;
    }

    public ReasonCode getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "DisconnectMessage [reason=" + reason + "]";
    }
}
