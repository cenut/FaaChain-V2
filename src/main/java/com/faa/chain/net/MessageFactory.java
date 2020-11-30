package com.faa.chain.net;

import com.faa.chain.consensus.messages.NewHeightMessage;
import com.faa.chain.consensus.messages.NewViewMessage;
import com.faa.chain.consensus.messages.ProposalMessage;
import com.faa.chain.consensus.messages.VoteMessage;
import com.faa.chain.exceptions.UnreachableException;
import com.faa.chain.net.msg.*;
import com.faa.chain.net.msg.consensus.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageFactory {
    private static final Logger logger = LoggerFactory.getLogger(MessageFactory.class);

    /**
     * Decode a raw message.
     *
     * @param code
     *            The message code
     * @param body
     *            The message body
     * @return The decoded message, or NULL if the message type is not unknown
     * @throws MessageException
     *             when the encoding is illegal
     */
    public Message create(byte code, byte[] body) throws MessageException {

        MessageCode c = MessageCode.of(code);
        if (c == null) {
            logger.debug("Invalid message code: " + code);
            return null;
        }

        try {
            switch (c) {
                case DISCONNECT:
                    return new DisconnectMessage(body);
                case PING:
                    return new PingMessage(body);
                case PONG:
                    return new PongMessage(body);
                case GET_NODES:
                    return new GetNodesMessage(body);
                case NODES:
                    return new NodesMessage(body);
                    /*
                case TRANSACTION:
                    return new TransactionMessage(body);

                 */
                case HANDSHAKE_INIT:
                    return new InitMessage(body);
                case HANDSHAKE_HELLO:
                    return new HelloMessage(body);
                case HANDSHAKE_WORLD:
                    return new WorldMessage(body);
                case GET_BLOCK:
                    return new GetBlockMessage(body);
                case BLOCK:
                    return new BlockMessage(body);
                case GET_BLOCK_HEADER:
                    return new GetBlockHeaderMessage(body);
                case BLOCK_HEADER:
                    return new BlockHeaderMessage(body);
                case GET_BLOCK_PARTS:
                    return new GetBlockPartsMessage(body);
                case BLOCK_PARTS:
                    return new BlockPartsMessage(body);
                case BFT_NEW_HEIGHT:
                    return new NewHeightMessage(body);
                case BFT_NEW_VIEW:
                    return new NewViewMessage(body);
                case BFT_PROPOSAL:
                    return new ProposalMessage(body);
                case BFT_VOTE:
                    return new VoteMessage(body);
                default:
                    throw new UnreachableException();
            }
        } catch (Exception e) {
            throw new MessageException("Failed to decode message", e);
        }
    }
}
