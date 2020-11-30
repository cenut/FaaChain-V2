package com.faa.chain.p2p;

import com.faa.utils.CommonUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import com.faa.chain.crypto.Key;
import com.faa.chain.net.FaaChannelInitializer;
import com.faa.chain.net.NodeManager.Node;
import com.faa.chain.utils.SystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * p2p client，异步连接远程p2p server节点
 */

@Component
public class PeerClient {

    private static final Logger logger = LoggerFactory.getLogger(PeerClient.class);

    private static final ThreadFactory factory = new ThreadFactory() {
        final AtomicInteger cnt = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "client-" + cnt.getAndIncrement());
        }
    };

    private int port;
    private Key coinbase;
    private EventLoopGroup workerGroup;

    private ScheduledFuture<?> ipRefreshFuture = null;
    private String ip;

    /**
     * Create a new PeerClient instance.
     */
    @PostConstruct
    public void init() {
        this.ip = CommonUtil.p2pMyIp().orElse(SystemUtil.getIp());
        this.port = CommonUtil.P2P_PORT;
        logger.info("Peer client info: ip = {}, port = {}", this.ip, this.port);
        this.coinbase = CommonUtil.coinbase;
        this.workerGroup = new NioEventLoopGroup(4, factory);
    }

    /**
     * Returns this node.
     *
     * @return
     */
    public Node getNode() {
        return new Node(ip, port);
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
     * Returns the listening IP port.
     *
     * @return
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the peerId of this client.
     *
     * @return
     */
    public String getPeerId() {
        return coinbase.toAddress();
    }

    /**
     * Returns the coinbase.
     *
     * @return
     */
    public Key getCoinbase() {
        return coinbase;
    }

    /**
     * Connects to a remote peer asynchronously.
     *
     * @param remoteNode
     * @return
     */
    public ChannelFuture connect(Node remoteNode, FaaChannelInitializer ci) {
        logger.info("PeerClient connecting to node: {}", remoteNode);
        Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);

        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT);
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CommonUtil.DEFAULT_CONNECT_TIMEOUT);
        b.remoteAddress(remoteNode.toAddress());

        b.handler(ci);

        return b.connect();
    }

    /**
     * Closes this client.
     */
    public void close() {
        logger.info("Shutting down PeerClient");

        workerGroup.shutdownGracefully();

        if (ipRefreshFuture != null) {
            ipRefreshFuture.cancel(true);
        }
    }
}
