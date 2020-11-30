package com.faa.chain.p2p;

import com.faa.utils.CommonUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import com.faa.chain.Starter;
import com.faa.chain.net.ConnectionLimitHandler;
import com.faa.chain.net.FaaChannelInitializer;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PeerServer {
    private final static Logger logger = LoggerFactory.getLogger(PeerServer.class);

    private static final ThreadFactory factory = new ThreadFactory() {
        final AtomicInteger cnt = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "server-" + cnt.getAndIncrement());
        }
    };

    @Autowired
    private BeanFactory beanFactory;

    protected Channel channel;

    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;

    public void start() {
        start(CommonUtil.P2P_IPADDRESS, CommonUtil.P2P_PORT);
    }

    public void start(String ip, int port) {
        if (isRunning()) {
            return;
        }

        try {
            bossGroup = new NioEventLoopGroup(1, factory);
            workerGroup = new NioEventLoopGroup(4, factory);

            ServerBootstrap b = new ServerBootstrap();

            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);

            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT);
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CommonUtil.DEFAULT_CONNECT_TIMEOUT);

            b.handler(new LoggingHandler());

            FaaChannelInitializer ci = beanFactory.getBean(FaaChannelInitializer.class);
            ci.setRemoteNode(null);
            b.childHandler(ci);

            logger.info("Starting peer server: address = {}:{}", ip, port);
            channel = b.bind(ip, port).sync().channel();
        } catch (Exception e) {
            logger.error("Failed to start peer server", e);
        }
    }

    public void stop() {
        if (isRunning() && channel.isOpen()) {
            try {
                channel.close().sync();

                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();

                ConnectionLimitHandler.reset();

                channel = null;
            } catch (Exception e) {
                logger.error("Failed to close channel", e);
            }
            logger.info("PeerServer shut down");
        }
    }

    public boolean isRunning() {
        return channel != null;
    }
}
