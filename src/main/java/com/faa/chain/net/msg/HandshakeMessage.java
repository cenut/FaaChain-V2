package com.faa.chain.net.msg;

import com.faa.utils.CommonUtil;
import com.faa.chain.crypto.Key;
import com.faa.chain.crypto.Sign;
import com.faa.chain.net.*;
import com.faa.chain.p2p.Peer;
import com.faa.chain.utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

public class HandshakeMessage extends Message {
    protected final Network network;
    protected final short networkVersion;

    protected final String peerId;
    protected final int port;

    protected final String clientId;
    protected final String[] capabilities;

    protected final int latestBlockNumber;

    protected final byte[] secret;
    protected final long timestamp;
    protected final Sign.SignatureData signature;

    public HandshakeMessage(MessageCode code, Class<?> responseMessageClass,
                            Network network, short networkVersion, String peerId, int port,
                            String clientId, String[] capabilities, int latestBlockNumber,
                            byte[] secret, Key coinbase) {
        super(code, responseMessageClass);

        this.network = network;
        this.networkVersion = networkVersion;
        this.peerId = peerId;
        this.port = port;
        this.clientId = clientId;
        this.capabilities = capabilities;
        this.latestBlockNumber = latestBlockNumber;
        this.secret = secret;
        this.timestamp = TimeUtil.currentTimeMillis();

        SimpleEncoder enc = encodeBasicInfo();
        this.signature = coinbase.sign(enc.toBytes());
        enc.writeBytes(signature.toBytes());

        this.body = enc.toBytes();
    }

    public HandshakeMessage(MessageCode code, Class<?> responseMessageClass, byte[] body) {
        super(code, responseMessageClass);

        SimpleDecoder dec = new SimpleDecoder(body);
        this.network = Network.of(dec.readByte());
        this.networkVersion = dec.readShort();
        this.peerId = dec.readString();
        this.port = dec.readInt();
        this.clientId = dec.readString();
        List<String> capabilities = new ArrayList<>();
        for (int i = 0, size = dec.readInt(); i < size; i++) {
            capabilities.add(dec.readString());
        }
        this.capabilities = capabilities.toArray(new String[0]);
        this.latestBlockNumber = dec.readInt();
        this.secret = dec.readBytes();
        this.timestamp = dec.readLong();
        this.signature = Sign.SignatureData.fromBytes(dec.readBytes());

        this.body = body;
    }

    protected SimpleEncoder encodeBasicInfo() {
        SimpleEncoder enc = new SimpleEncoder();

        enc.writeByte(network.id());
        enc.writeShort(networkVersion);
        enc.writeString(peerId);
        enc.writeInt(port);
        enc.writeString(clientId);
        enc.writeInt(capabilities.length);
        for (String capability : capabilities) {
            enc.writeString(capability);
        }
        enc.writeInt(latestBlockNumber);
        enc.writeBytes(secret);
        enc.writeLong(timestamp);

        return enc;
    }

    /**
     * Validates this HELLO message.
     *
     * <p>
     * NOTE: only data format and signature is checked here.
     * </p>
     *
     * @return true if success, otherwise false
     */
    public boolean validate() {
        if (network == CommonUtil.network
                && networkVersion == CommonUtil.networkVersion
                && peerId != null && peerId.length() == 44
                && port > 0 && port <= 65535
                && clientId != null && clientId.length() < 128
                && latestBlockNumber >= 0
                && secret != null && secret.length == InitMessage.SECRET_LENGTH
                && Math.abs(TimeUtil.currentTimeMillis() - timestamp) <= CommonUtil.netHandshakeExpiry
                && signature != null) {

            SimpleEncoder enc = encodeBasicInfo();
            return Key.verify(enc.toBytes(), signature, peerId);
        } else {
            return false;
        }
    }

    /**
     * Constructs a Peer object from the handshake info.
     *
     * @param ip
     * @return
     */
    public Peer getPeer(String ip) {
        return new Peer(network, networkVersion, peerId, ip, port, clientId, capabilities, latestBlockNumber);
    }

    /**
     * Returns the secret.
     *
     * @return
     */
    public byte[] getSecret() {
        return secret;
    }
}
