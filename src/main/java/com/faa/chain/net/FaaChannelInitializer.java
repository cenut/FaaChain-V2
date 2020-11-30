package com.faa.chain.net;

import com.faa.utils.CommonUtil;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.socket.nio.NioSocketChannel;
import com.faa.chain.Starter;
import com.faa.chain.net.NodeManager.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;

/**
 * netty channel初始化类
 */

@Component
@Scope("prototype")
public class FaaChannelInitializer extends ChannelInitializer<NioSocketChannel>  {

    private final static Logger logger = LoggerFactory.getLogger(FaaChannelInitializer.class);

    @Autowired
    private BeanFactory beanFactory;

    @Resource
    private Starter starter;
    @Resource
    private ChannelManager channelMgr;

    private Node remoteNode;

    public void setRemoteNode(Node remoteNode) {
        this.remoteNode = remoteNode;
    }

    @Override
    public void initChannel(NioSocketChannel ch) throws Exception {
        try {
            InetSocketAddress address = isServerMode() ? ch.remoteAddress() : remoteNode.toAddress();
            logger.info("New {} channel: remoteAddress = {}:{}", isServerMode() ? "inbound" : "outbound",
                    address.getAddress().getHostAddress(), address.getPort());

            if (isServerMode() && !channelMgr.isAcceptable(address)) {
                logger.warn("Disallowed {} connection: {}", isServerMode() ? "inbound" : "outbound",
                        address.toString());
                ch.disconnect();
                return;
            }

            Channel channel = beanFactory.getBean(Channel.class);
            channel.init(ch.pipeline(), isServerMode(), address, starter);
            channelMgr.add(channel);

            int bufferSize = Frame.HEADER_SIZE + CommonUtil.netMaxFrameBodySize;
            ch.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(bufferSize));
            ch.config().setOption(ChannelOption.SO_RCVBUF, bufferSize);
            ch.config().setOption(ChannelOption.SO_BACKLOG, 1024);

            ch.closeFuture().addListener(future -> {
                channelMgr.remove(channel);
            });
        } catch (Exception e) {
            logger.error("Exception in channel initializer", e);
        }
    }

    /**
     * Returns whether is in server mode.
     *
     * @return
     */
    public boolean isServerMode() {
        return remoteNode == null;
    }
}
