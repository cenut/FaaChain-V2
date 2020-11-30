package com.faa.chain.net;

import com.faa.utils.CommonUtil;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import com.faa.chain.Starter;
import com.faa.chain.p2p.FaaP2pHandler;
import com.faa.chain.p2p.Peer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@Component
@Scope("prototype")
public class Channel {

    @Autowired
    private BeanFactory beanFactory;

    private boolean isInbound;
    private InetSocketAddress remoteAddress;
    private Peer remotePeer;

    private MessageQueue msgQueue;

    private boolean isActive;

    /**
     * Initializes this channel.
     *
     * @param pipe
     * @param isInbound
     * @param remoteAddress
     */
    public void init(ChannelPipeline pipe, boolean isInbound, InetSocketAddress remoteAddress, Starter starter) {
        this.isInbound = isInbound;
        this.remoteAddress = remoteAddress;
        this.remotePeer = null;

        this.msgQueue = new MessageQueue();

        // 注册 channel handlers，收到的event后按顺序执行Handler处理消息
        if (isInbound) {
            pipe.addLast("inboundLimitHandler",
                    new ConnectionLimitHandler(CommonUtil.netMaxInboundConnectionsPerIp));
        }
        pipe.addLast("readTimeoutHandler",
                new ReadTimeoutHandler(CommonUtil.netChannelIdleTimeout, TimeUnit.MILLISECONDS));
        pipe.addLast("frameHandler", new FaaFrameHandler());
        pipe.addLast("messageHandler", new FaaMessageHandler());

        FaaP2pHandler p2pHandler = beanFactory.getBean(FaaP2pHandler.class);
        p2pHandler.setChannel(this);
        pipe.addLast("p2pHandler", p2pHandler);
    }

    /**
     * Returns the message queue.
     *
     * @return
     */
    public MessageQueue getMessageQueue() {
        return msgQueue;
    }

    /**
     * Returns whether this is an inbound channel.
     *
     * @return
     */
    public boolean isInbound() {
        return isInbound;
    }

    /**
     * Returns whether this is an outbound channel.
     *
     * @return
     */
    public boolean isOutbound() {
        return !isInbound();
    }

    /**
     * Returns the remote peer.
     *
     * @return
     */
    public Peer getRemotePeer() {
        return remotePeer;
    }

    /**
     * Returns whether this channel is active.
     *
     * @return
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Sets this channel to be active.
     *
     * @param remotePeer
     */
    public void setActive(Peer remotePeer) {
        this.remotePeer = remotePeer;
        this.isActive = true;
    }

    /**
     * Sets this channel to be inactive.
     */
    public void setInactive() {
        this.isActive = false;
    }

    /**
     * Returns the remote address.
     *
     * @return
     */
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    /**
     * Returns remote IP address.
     *
     * @return
     */
    public String getRemoteIp() {
        return remoteAddress.getAddress().getHostAddress();
    }

    /**
     * Returns remote port.
     *
     * @return
     */
    public int getRemotePort() {
        return remoteAddress.getPort();
    }

    @Override
    public String toString() {
        return "Channel [" + (isInbound ? "Inbound" : "Outbound") + ", remoteIp = " + getRemoteIp() + ", remotePeer = "
                + remotePeer + "]";
    }
}
