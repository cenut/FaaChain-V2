package com.faa.chain.net.msg;

import com.faa.chain.net.Message;
import com.faa.chain.net.MessageCode;
import com.faa.chain.net.NodeManager.Node;
import com.faa.chain.net.SimpleDecoder;
import com.faa.chain.net.SimpleEncoder;

import java.util.ArrayList;
import java.util.List;

public class NodesMessage extends Message {

    public static final int MAX_NODES = 256;

    private final List<Node> nodes;

    /**
     * Create a NODES message.
     *
     * @param nodes
     */
    public NodesMessage(List<Node> nodes) {
        super(MessageCode.NODES, null);

        this.nodes = nodes;

        SimpleEncoder enc = new SimpleEncoder();
        enc.writeInt(nodes.size());
        for (Node n : nodes) {
            enc.writeString(n.getIp());
            enc.writeInt(n.getPort());
        }
        this.body = enc.toBytes();
    }

    /**
     * Parse a NODES message from byte array.
     *
     * @param body
     */
    public NodesMessage(byte[] body) {
        super(MessageCode.NODES, null);

        this.nodes = new ArrayList<>();
        SimpleDecoder dec = new SimpleDecoder(body);
        for (int i = 0, size = dec.readInt(); i < size; i++) {
            String host = dec.readString();
            int port = dec.readInt();
            nodes.add(new Node(host, port));
        }

        this.body = body;
    }

    public boolean validate() {
        return nodes != null && nodes.size() <= MAX_NODES;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return "NodesMessage [# nodes =" + nodes.size() + "]";
    }
}
