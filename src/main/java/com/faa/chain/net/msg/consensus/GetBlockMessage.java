package com.faa.chain.net.msg.consensus;

import com.faa.chain.net.Message;
import com.faa.chain.net.MessageCode;
import com.faa.chain.net.SimpleDecoder;
import com.faa.chain.net.SimpleEncoder;

public class GetBlockMessage extends Message {

    private final int number;

    public GetBlockMessage(int number) {
        super(MessageCode.GET_BLOCK, BlockMessage.class);
        this.number = number;

        SimpleEncoder enc = new SimpleEncoder();
        enc.writeInt(number);
        this.body = enc.toBytes();
    }

    public GetBlockMessage(byte[] body) {
        super(MessageCode.GET_BLOCK, BlockMessage.class);

        SimpleDecoder dec = new SimpleDecoder(body);
        this.number = dec.readInt();

        this.body = body;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return "GetBlockMessage [number=" + number + "]";
    }
}
