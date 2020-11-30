package com.faa.chain.net.msg.consensus;

import com.faa.chain.core.Block;
import com.faa.chain.net.Message;
import com.faa.chain.net.MessageCode;

public class BlockMessage extends Message {

    private final Block block;

    public BlockMessage(Block block) {
        super(MessageCode.BLOCK, null);

        this.block = block;

        this.body = block.toBytes();
    }

    public BlockMessage(byte[] body) {
        super(MessageCode.BLOCK, null);

        this.block = Block.fromBytes(body);

        this.body = body;
    }

    public Block getBlock() {
        return block;
    }

    @Override
    public String toString() {
        return "BlockMessage [block=" + block + "]";
    }
}
