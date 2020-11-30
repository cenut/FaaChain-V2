package com.faa.chain.net.msg.consensus;

import com.faa.chain.net.Message;
import com.faa.chain.net.MessageCode;
import com.faa.chain.net.SimpleDecoder;
import com.faa.chain.net.SimpleEncoder;

public class GetBlockPartsMessage  extends Message {

    private final int number;
    private final int parts;

    public GetBlockPartsMessage(int number, int parts) {
        super(MessageCode.GET_BLOCK_PARTS, BlockPartsMessage.class);

        this.number = number;
        this.parts = parts;

        SimpleEncoder enc = new SimpleEncoder();
        enc.writeInt(number);
        enc.writeInt(parts);
        this.body = enc.toBytes();
    }

    public GetBlockPartsMessage(byte[] body) {
        super(MessageCode.GET_BLOCK_PARTS, BlockPartsMessage.class);

        SimpleDecoder dec = new SimpleDecoder(body);
        this.number = dec.readInt();
        this.parts = dec.readInt();

        this.body = body;
    }

    public int getNumber() {
        return number;
    }

    public int getParts() {
        return parts;
    }

    @Override
    public String toString() {
        return "GetBlockPartsMessage [number=" + number + ", parts = " + parts + "]";
    }
}
