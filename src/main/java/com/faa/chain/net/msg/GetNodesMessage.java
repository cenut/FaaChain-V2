package com.faa.chain.net.msg;

import com.faa.chain.net.Message;
import com.faa.chain.net.MessageCode;
import com.faa.chain.net.SimpleEncoder;

public class GetNodesMessage extends Message {

    /**
     * Create a GET_NODES message.
     *
     */
    public GetNodesMessage() {
        super(MessageCode.GET_NODES, NodesMessage.class);

        SimpleEncoder enc = new SimpleEncoder();
        this.body = enc.toBytes();
    }

    /**
     * Parse a GET_NODES message from byte array.
     *
     * @param body
     */
    public GetNodesMessage(byte[] body) {
        super(MessageCode.GET_NODES, NodesMessage.class);

        this.body = body;
    }

    @Override
    public String toString() {
        return "GetNodesMessage";
    }
}
