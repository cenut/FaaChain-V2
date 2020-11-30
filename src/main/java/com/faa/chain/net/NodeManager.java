package com.faa.chain.net;

import com.faa.utils.CommonUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.faa.chain.Starter;
import com.faa.chain.p2p.PeerClient;
import com.faa.chain.utils.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * 节点管理类，节点包含peer client，channel，启动后每1.5s会获取active channel然后连接到活跃的Channel
 * p2p握手完成后，通过ChannelMgr.onChannelActive方法将新加入节点添加到活跃的Channel列表
 */

@Component
public class NodeManager {

    private static final Logger logger = LoggerFactory.getLogger(NodeManager.class);

    private static final ThreadFactory factory = new ThreadFactory() {
        private final AtomicInteger cnt = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "node-" + cnt.getAndIncrement());
        }
    };

    @Autowired
    private BeanFactory beanFactory;

    private static final long MAX_QUEUE_SIZE = 1024;
    private static final int LRU_CACHE_SIZE = 1024;
    private static final long RECONNECT_WAIT = 60L * 1000L;

    @Resource
    private ChannelManager channelMgr;
    @Resource
    private PeerClient client;

    private final Deque<Node> deque = new ConcurrentLinkedDeque<>();

    private final Cache<Node, Long> lastConnect = Caffeine.newBuilder().maximumSize(LRU_CACHE_SIZE).build();

    private ScheduledExecutorService exec;
    private ScheduledFuture<?> connectFuture;
    private ScheduledFuture<?> fetchFuture;

    private volatile boolean isRunning;

    @PostConstruct
    void init() {
        this.exec = Executors.newSingleThreadScheduledExecutor(factory);
    }

    /**
     * 启动node manager
     */
    public synchronized void start() {
        if (!isRunning) {
            addNodes(CommonUtil.p2pSeedNodes);

            connectFuture = exec.scheduleAtFixedRate(this::doConnect, 1000, 500, TimeUnit.MILLISECONDS);
            fetchFuture = exec.scheduleAtFixedRate(this::doFetch, 5, 100, TimeUnit.SECONDS);

            isRunning = true;
            logger.info("Node manager started");
        }
    }

    /**
     * 停止node manager.
     */
    public synchronized void stop() {
        if (isRunning) {
            connectFuture.cancel(true);
            fetchFuture.cancel(false);

            isRunning = false;
            logger.info("Node manager stopped");
        }
    }

    /**
     * Returns if the node manager is running or not.
     *
     * @return true if running, otherwise false
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Add a node to the connection queue.
     *
     * @param node
     */
    public void addNode(Node node) {
        deque.addFirst(node);
        while (queueSize() > MAX_QUEUE_SIZE) {
            deque.removeLast();
        }
    }

    /**
     * Add a collection of nodes to the connection queue.
     *
     * @param nodes
     */
    public void addNodes(Collection<Node> nodes) {
        for (Node node : nodes) {
            addNode(node);
        }
    }

    /**
     * Get the connection queue size.
     *
     * @return
     */
    public int queueSize() {
        return deque.size();
    }

    /**
     * 从netDnsSeedsMainNet获取seed nodes列表.
     *
     * @param network
     * @return
     */
    public Set<Node> getSeedNodes(Network network) {
        Set<Node> nodes = new HashSet<>();

        List<String> names;
        switch (network) {
            case MAINNET:
                names = CommonUtil.netDnsSeedsMainNet;
                break;
            default:
                return nodes;
        }

        names.parallelStream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(name -> {
                    try {
                        return InetAddress.getAllByName(name);
                    } catch (UnknownHostException e) {
                        logger.warn("Failed to get seed nodes from {}", name);
                        return new InetAddress[0];
                    }
                })
                .flatMap(Stream::of)
                .forEach(address -> nodes.add(new Node(address.getHostAddress(), CommonUtil.P2P_PORT)));

        return nodes;
    }

    /**
     * Connect to a node in the queue.
     */
    protected void doConnect() {
        Set<InetSocketAddress> activeAddresses = channelMgr.getActiveAddresses();
        Node node;

        while ((node = deque.pollFirst()) != null && channelMgr.size() < CommonUtil.NET_MAX_OUT_BOUND_CONNECTIONS) {
            Long lastTouch = lastConnect.getIfPresent(node);
            long now = TimeUtil.currentTimeMillis();

            if (!client.getNode().equals(node) // self
                    && !(Objects.equals(node.getIp(), client.getIp()) && node.getPort() == client.getPort()) // self
                    && !activeAddresses.contains(node.toAddress()) // connected
                    && (lastTouch == null || lastTouch + RECONNECT_WAIT < now)) {

                FaaChannelInitializer ci = beanFactory.getBean(FaaChannelInitializer.class);
                ci.setRemoteNode(node);
                client.connect(node, ci);
                lastConnect.put(node, now);
                break;
            }
        }
    }

    /**
     * Fetches seed nodes from DNS records or configuration.
     */
    protected void doFetch() {
        addNodes(getSeedNodes(CommonUtil.network));
    }

    /**
     * 表示faachain网络里面的一个逻辑节点.
     */
    public static class Node {

        private final InetSocketAddress address;

        /**
         * Construct a node with the given socket address.
         *
         * @param address
         */
        public Node(InetSocketAddress address) {
            this.address = address;
        }

        /**
         * Construct a node with the given IP address and port.
         *
         * @param ip
         * @param port
         */
        public Node(InetAddress ip, int port) {
            this(new InetSocketAddress(ip, port));
        }

        /**
         * Construct a node with the given IP address and port.
         *
         * @param ip
         *            IP address, or hostname (not encouraged to use)
         * @param port
         *            port number
         */
        public Node(String ip, int port) {
            this(new InetSocketAddress(ip, port));
        }

        /**
         * Returns the IP address.
         *
         * @return
         */
        public String getIp() {
            return address.getAddress().getHostAddress();
        }

        /**
         * Returns the port number
         *
         * @return
         */
        public int getPort() {
            return address.getPort();
        }

        /**
         * Converts into a socket address.
         *
         * @return
         */
        public InetSocketAddress toAddress() {
            return address;
        }

        @Override
        public int hashCode() {
            return address.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Node && address.equals(((Node) o).toAddress());
        }

        @Override
        public String toString() {
            return getIp() + ":" + getPort();
        }
    }
}
