package com.faa.chain.net.msg;

import com.faa.chain.crypto.Key;
import com.faa.chain.net.MessageCode;
import com.faa.chain.net.Network;

import java.util.Arrays;

public class WorldMessage extends HandshakeMessage {
    public WorldMessage(Network network, short networkVersion, String peerId, int port,
                        String clientId, String[] capabilities, int latestBlockNumber,
                        byte[] secret, Key coinbase) {
        super(MessageCode.HANDSHAKE_WORLD, null, network, networkVersion, peerId, port, clientId,
              capabilities, latestBlockNumber, secret, coinbase);
    }

    public WorldMessage(byte[] encoded) {
        super(MessageCode.HANDSHAKE_WORLD, null, encoded);
    }

    @Override
    public String toString() {
        return "WorldMessage{" +
                "network=" + network +
                ", networkVersion=" + networkVersion +
                ", peerId='" + peerId + '\'' +
                ", port=" + port +
                ", clientId='" + clientId + '\'' +
                ", capabilities=" + Arrays.toString(capabilities) +
                ", latestBlockNumber=" + latestBlockNumber +
                ", secret=" + secret +
                ", timestamp=" + timestamp +
                '}';
    }
}
