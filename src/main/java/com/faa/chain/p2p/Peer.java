package com.faa.chain.p2p;

import com.faa.chain.net.Network;

public class Peer {

    /**
     * The network id;
     */
    private final Network network;

    /**
     * The network version.
     */
    private final short networkVersion;

    /**
     * The peer id.
     */
    private final String peerId;

    /**
     * The IP address.
     */
    private final String ip;

    /**
     * The listening port.
     */
    private final int port;

    /**
     * The client software id.
     */
    private final String clientId;

    /**
     * The supported capabilities.
     */
    private final String[] capabilities;

    private int latestBlockNumber;
    private long latency;

    /**
     * Create a new Peer instance.
     *
     * @param network
     * @param networkVersion
     * @param peerId
     * @param ip
     * @param port
     * @param clientId
     * @param capabilities
     * @param latestBlockNumber
     */
    public Peer(Network network, short networkVersion, String peerId, String ip, int port, String clientId,
                String[] capabilities, int latestBlockNumber) {
        this.network = network;
        this.ip = ip;
        this.port = port;
        this.peerId = peerId;
        this.networkVersion = networkVersion;
        this.clientId = clientId;
        this.capabilities = capabilities;
        this.latestBlockNumber = latestBlockNumber;
    }

    /**
     * Returns the listening IP address.
     *
     * @return
     */
    public String getIp() {
        return ip;
    }

    /**
     * Returns the listening port number.
     *
     * @return
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the network.
     *
     * @return
     */
    public Network getNetwork() {
        return network;
    }

    /**
     * Returns the network version.
     *
     * @return
     */
    public short getNetworkVersion() {
        return networkVersion;
    }

    /**
     * Returns the client id.
     *
     * @return
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Returns the peerId.
     *
     * @return
     */
    public String getPeerId() {
        return peerId;
    }

    /**
     * Returns the capabilities.
     */
    public String[] getCapabilities() {
        return capabilities;
    }

    /**
     * Returns the latest block number.
     *
     * @return
     */
    public int getLatestBlockNumber() {
        return latestBlockNumber;
    }

    /**
     * Sets the latest block number.
     *
     * @param number
     */
    public void setLatestBlockNumber(int number) {
        this.latestBlockNumber = number;
    }

    /**
     * Returns peer latency.
     *
     * @return
     */
    public long getLatency() {
        return latency;
    }

    /**
     * Sets peer latency.
     *
     * @param latency
     */
    public void setLatency(long latency) {
        this.latency = latency;
    }

    @Override
    public String toString() {
        return getPeerId() + "@" + ip + ":" + port;
    }
}
