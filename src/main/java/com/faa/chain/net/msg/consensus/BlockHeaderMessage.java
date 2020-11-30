package com.faa.chain.net.msg.consensus;

import com.faa.chain.core.BlockHeader;
import com.faa.chain.net.Message;
import com.faa.chain.net.MessageCode;

public class BlockHeaderMessage extends Message {

    private final BlockHeader header;

    public BlockHeaderMessage(BlockHeader header) {
        super(MessageCode.BLOCK_HEADER, null);

        this.header = header;

        this.body = header.toBytes();
    }

    public BlockHeaderMessage(byte[] body) {
        super(MessageCode.BLOCK_HEADER, null);

        this.header = BlockHeader.fromBytes(body);

        this.body = body;
    }

    public BlockHeader getHeader() {
        return header;
    }

    @Override
    public String toString() {
        return "BlockHeaderMessage [header=" + header + "]";
    }
}
