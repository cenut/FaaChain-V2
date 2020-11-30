package com.faa.chain.net;

import com.faa.chain.net.filter.FaaIpFilter;
import com.faa.chain.p2p.Peer;
import com.faa.service.UnionNodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChannelManager {

    private final static Logger logger = LoggerFactory.getLogger(ChannelManager.class);

    protected ConcurrentHashMap<InetSocketAddress, Channel> channels = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<String, Channel> activeChannels = new ConcurrentHashMap<>();

    protected FaaIpFilter ipFilter;

    @Resource
    private UnionNodeService unionNodeService;

    @PostConstruct
    public void initChannelManager() throws UnknownHostException {
        this.updateIpFilter();
    }

    private void updateIpFilter() throws UnknownHostException {
        List<InetSocketAddress> whiteList = unionNodeService.listAllUnionNode();
        ipFilter = new FaaIpFilter(whiteList);
    }

    /**
     * Returns whether a socket address is connected.
     *
     * @param address
     * @return
     */
    public boolean isConnected(InetSocketAddress address) {
        return channels.containsKey(address);
    }

    /**
     * Returns whether the specified IP is connected.
     *
     * @param ip
     * @return
     */
    public boolean isActiveIP(String ip) {
        for (Channel c : activeChannels.values()) {
            if (c.getRemoteIp().equals(ip)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns whether the specified peer is connected.
     *
     * @param peerId
     * @return
     */
    public boolean isActivePeer(String peerId) {
        return activeChannels.containsKey(peerId);
    }

    /**
     * Returns the number of channels.
     *
     * @return
     */
    public int size() {
        return channels.size();
    }

    /**
     * Adds a new channel to this manager.
     *
     * @param ch
     *            channel instance
     */
    public void add(Channel ch) {
        logger.debug("Channel added: remoteAddress = {}:{}", ch.getRemoteIp(), ch.getRemotePort());

        channels.put(ch.getRemoteAddress(), ch);
    }

    /**
     * Returns whether a connection from the given address is acceptable or not.
     *
     * @param address
     * @return
     */
    public boolean isAcceptable(InetSocketAddress address) throws UnknownHostException {
        // 判断之前，更新白名单列表
        this.updateIpFilter();
        return ipFilter == null || ipFilter.isAcceptable(address);
    }

    /**
     * Removes a disconnected channel from this manager.
     *
     * @param ch
     *            channel instance
     */
    public void remove(Channel ch) {
        logger.debug("Channel removed: remoteAddress = {}:{}", ch.getRemoteIp(), ch.getRemotePort());

        channels.remove(ch.getRemoteAddress());
        if (ch.isActive()) {
            activeChannels.remove(ch.getRemotePeer().getPeerId());
            ch.setInactive();
        }
    }

    /**
     * When a channel becomes active.
     *
     * @param channel
     * @param peer
     */
    public void onChannelActive(Channel channel, Peer peer) {
        channel.setActive(peer);
        activeChannels.put(peer.getPeerId(), channel);
    }

    /**
     * Returns a copy of the active peers.
     *
     * @return
     */
    public List<Peer> getActivePeers() {
        List<Peer> list = new ArrayList<>();

        for (Channel c : activeChannels.values()) {
            list.add(c.getRemotePeer());
        }

        return list;
    }

    /**
     * Returns the listening IP addresses of active peers.
     *
     * @return
     */
    public Set<InetSocketAddress> getActiveAddresses() {
        Set<InetSocketAddress> set = new HashSet<>();

        for (Channel c : activeChannels.values()) {
            Peer p = c.getRemotePeer();
            set.add(new InetSocketAddress(p.getIp(), p.getPort()));
        }

        return set;
    }

    /**
     * Returns the active channels.
     *
     * @return
     */
    public List<Channel> getActiveChannels() {

        return new ArrayList<>(activeChannels.values());
    }

    /**
     * Returns the active channels, filtered by peerId.
     *
     * @param peerIds
     *            peerId filter
     * @return
     */
    public List<Channel> getActiveChannels(List<String> peerIds) {
        List<Channel> list = new ArrayList<>();

        for (String peerId : peerIds) {
            if (activeChannels.containsKey(peerId)) {
                list.add(activeChannels.get(peerId));
            }
        }

        return list;
    }


    /**
     * Returns the active channels, whose message queue is idle.
     *
     * @return
     */
    public List<Channel> getIdleChannels() {
        List<Channel> list = new ArrayList<>();

        for (Channel c : activeChannels.values()) {
            if (c.getMessageQueue().isIdle()) {
                list.add(c);
            }
        }

        return list;
    }
}
