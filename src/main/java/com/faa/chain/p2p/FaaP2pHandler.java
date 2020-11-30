package com.faa.chain.p2p;

import com.faa.chain.consensus.messages.NewHeightMessage;
import com.faa.chain.core.Block;
import com.faa.chain.core.BlockHeader;
import com.faa.chain.core.BlockPart;
import com.faa.chain.core.SyncManager;
import com.faa.chain.exceptions.UnreachableException;
import com.faa.chain.net.msg.consensus.*;
import com.faa.service.BftService;
import com.faa.service.BlockService;
import com.faa.service.UnionNodeService;
import com.faa.utils.CommonUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import com.faa.chain.Starter;
import com.faa.chain.net.*;
import com.faa.chain.net.msg.*;
import com.faa.chain.utils.Bytes;
import com.faa.chain.utils.TimeUtil;
import com.faa.chain.net.NodeManager.Node;
import jdk.nashorn.internal.objects.annotations.Constructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.faa.chain.net.msg.NodesMessage.MAX_NODES;

@Component
@Scope("prototype")
public class FaaP2pHandler extends SimpleChannelInboundHandler<Message> {

    private final static Logger logger = LoggerFactory.getLogger(FaaP2pHandler.class);

    private static final ScheduledExecutorService exec = Executors
            .newSingleThreadScheduledExecutor(new ThreadFactory() {
                private final AtomicInteger cnt = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "p2p-" + cnt.getAndIncrement());
                }
            });

    private Channel channel;
    @Resource
    private Starter starter;
    @Resource
    private ChannelManager channelMgr;
    @Resource
    private NodeManager nodeMgr;
    @Resource
    private PendingManager pendingMgr;
    @Resource
    private PeerClient client;
    @Resource
    private UnionNodeService unionNodeService;
    @Resource
    private BftService bftService;
    @Resource
    private BlockService blockService;
    @Resource
    private SyncManager sync;
    private MessageQueue msgQueue;

    private AtomicBoolean isHandshakeDone = new AtomicBoolean(false);

    private ScheduledFuture<?> getNodes = null;
    private ScheduledFuture<?> pingPong = null;

    private byte[] secret = Bytes.random(InitMessage.SECRET_LENGTH);
    private long timestamp = TimeUtil.currentTimeMillis();

    public void setChannel(Channel channel) {
        this.channel = channel;
        this.msgQueue = channel.getMessageQueue();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 程序运行会执行channelActive函数
        logger.info("P2P handler active, remoteIp = {}", channel.getRemoteIp());

        // activate message queue
        msgQueue.activate(ctx);

        // disconnect if too many connections
        if (channel.isInbound() && channelMgr.size() >= CommonUtil.netMaxInboundConnections) {
            msgQueue.disconnect(ReasonCode.TOO_MANY_PEERS);
            return;
        }

        if (channel.isInbound()) {
            msgQueue.sendMessage(new InitMessage(secret, timestamp));
        }

        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("P2P handler inactive, remoteIp = {}", channel.getRemoteIp());

        msgQueue.deactivate();

        if (getNodes != null) {
            getNodes.cancel(false);
            getNodes = null;
        }

        if (pingPong != null) {
            pingPong.cancel(false);
            pingPong = null;
        }

        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.info("Exception in P2P handler, remoteIp = {}", channel.getRemoteIp(), cause);

        ctx.close();
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, Message msg) throws InterruptedException {
        logger.info("Received message: {}", msg.toString());

        switch (msg.getCode()) {
            /* p2p */
            case DISCONNECT:
                onDisconnect(ctx, (DisconnectMessage) msg);
                break;
            case PING:
                onPing();
                break;
            case PONG:
                onPong();
                break;
            case GET_NODES:
                onGetNodes();
                break;
            case NODES:
                onNodes((NodesMessage) msg);
                break;
            case TRANSACTION:
                onTransaction((TransactionMessage) msg);
                break;
            case HANDSHAKE_INIT:
                onHandshakeInit((InitMessage) msg);
                break;
            case HANDSHAKE_HELLO:
                onHandshakeHello((HelloMessage) msg);
                break;
            case HANDSHAKE_WORLD:
                onHandshakeWorld((WorldMessage) msg);
                break;

            /* sync */
            case GET_BLOCK:
            case BLOCK:
            case GET_BLOCK_HEADER:
            case BLOCK_HEADER:
            case GET_BLOCK_PARTS:
            case BLOCK_PARTS:
                onSync(msg);
                break;

            /* bft */
            case BFT_NEW_HEIGHT:
            case BFT_NEW_VIEW:
            case BFT_PROPOSAL:
            case BFT_VOTE:
                onBft(msg);
                break;

            default:
                ctx.fireChannelRead(msg);
                break;
        }
    }

    protected void onDisconnect(ChannelHandlerContext ctx, DisconnectMessage msg) {
        ReasonCode reason = msg.getReason();
        logger.info("Received a DISCONNECT message: reason = {}, remoteIP = {}",
                reason, channel.getRemoteIp());

        ctx.close();
    }

    private long lastPing;

    protected void onPing() {
        PongMessage pong = new PongMessage();
        msgQueue.sendMessage(pong);
        lastPing = TimeUtil.currentTimeMillis();
    }

    protected void onPong() {
        if (lastPing > 0) {
            long latency = TimeUtil.currentTimeMillis() - lastPing;
            channel.getRemotePeer().setLatency(latency);
        }
    }

    protected void onGetNodes() {
        List<InetSocketAddress> activeAddresses = unionNodeService.listAllUnionNode();
        Collections.shuffle(activeAddresses);
        NodesMessage nodesMsg = new NodesMessage(activeAddresses.stream()
                .limit(MAX_NODES).map(Node::new).collect(Collectors.toList()));
        msgQueue.sendMessage(nodesMsg);
    }

    protected void onNodes(NodesMessage msg) {
        if (msg.validate()) {
            nodeMgr.addNodes(msg.getNodes());
        }
    }

    protected void onTransaction(TransactionMessage msg) {
        pendingMgr.addTransaction(msg.getTransaction());
    }

    protected void onHandshakeInit(InitMessage msg) {
        if (channel.isInbound()) {
            return;
        }

        if (!msg.validate()) {
            this.msgQueue.disconnect(ReasonCode.INVALID_HANDSHAKE);
            return;
        }

        this.secret = msg.getSecret();
        this.timestamp = msg.getTimestamp();

        this.msgQueue.sendMessage(new HelloMessage(CommonUtil.network, CommonUtil.networkVersion, client.getPeerId(),
                client.getPort(), CommonUtil.getClientId(), CommonUtil.getClientCapabilities().toArray(),
                1, secret, client.getCoinbase()));
    }

    protected void onHandshakeHello(HelloMessage msg) {
        if (channel.isOutbound()) {
            return;
        }
        Peer peer = msg.getPeer(channel.getRemoteIp());

        ReasonCode code = checkPeer(peer, true);
        if (code != null) {
            msgQueue.disconnect(code);
            return;
        }

        if (!Arrays.equals(secret, msg.getSecret()) || !msg.validate()) {
            msgQueue.disconnect(ReasonCode.INVALID_HANDSHAKE);
            return;
        }

        this.msgQueue.sendMessage(new WorldMessage(CommonUtil.network, CommonUtil.networkVersion, client.getPeerId(),
                client.getPort(), CommonUtil.getClientId(), CommonUtil.getClientCapabilities().toArray(),
                1, secret, client.getCoinbase()));

        onHandshakeDone(peer);
    }

    protected void onHandshakeWorld(WorldMessage msg) {
        if (channel.isInbound()) {
            return;
        }
        Peer peer = msg.getPeer(channel.getRemoteIp());

        ReasonCode code = checkPeer(peer, true);
        if (code != null) {
            msgQueue.disconnect(code);
            return;
        }

        if (!Arrays.equals(secret, msg.getSecret()) || !msg.validate()) {
            msgQueue.disconnect(ReasonCode.INVALID_HANDSHAKE);
            return;
        }

        onHandshakeDone(peer);
    }

    protected void onSync(Message msg) {
        if (!isHandshakeDone.get()) {
            return;
        }

        switch (msg.getCode()) {
            case GET_BLOCK: {
                GetBlockMessage m = (GetBlockMessage) msg;
                Block block = blockService.getBlock(m.getNumber());
                channel.getMessageQueue().sendMessage(new BlockMessage(block));
                break;
            }
            case GET_BLOCK_HEADER: {
                GetBlockHeaderMessage m = (GetBlockHeaderMessage) msg;
                BlockHeader header = blockService.getBlockHeader(m.getNumber());
                channel.getMessageQueue().sendMessage(new BlockHeaderMessage(header));
                break;
            }
            case GET_BLOCK_PARTS: {
                GetBlockPartsMessage m = (GetBlockPartsMessage) msg;
                int number = m.getNumber();
                int parts = m.getParts();

                List<byte[]> partsSerialized = new ArrayList<>();
                Block block = blockService.getBlock(number);
                for (BlockPart part : BlockPart.decode(parts)) {
                    switch (part) {
                        case HEADER:
                            partsSerialized.add(block.getEncodedHeader());
                            break;
                        case TRANSACTIONS:
                            partsSerialized.add(block.getEncodedTransactions());
                            break;
                        case VOTES:
                            partsSerialized.add(block.getEncodedVotes());
                            break;
                        default:
                            throw new UnreachableException();
                    }
                }

                channel.getMessageQueue().sendMessage(new BlockPartsMessage(number, parts, partsSerialized));
                break;
            }
            case BLOCK:
            case BLOCK_HEADER:
            case BLOCK_PARTS: {
                sync.onMessage(channel, msg);
                break;
            }
            default:
                throw new UnreachableException();
        }
    }

    protected void onBft(Message msg) {
        if (!isHandshakeDone.get()) {
            return;
        }

        bftService.onMessage(channel, msg);
    }

    private ReasonCode checkPeer(Peer peer, boolean newHandShake) {
        if (newHandShake && !CommonUtil.network.equals(peer.getNetwork())) {
            return ReasonCode.BAD_NETWORK;
        }
        if (CommonUtil.networkVersion != peer.getNetworkVersion()) {
            return ReasonCode.BAD_NETWORK_VERSION;
        }
        if (client.getPeerId().equals(peer.getPeerId()) || channelMgr.isActivePeer(peer.getPeerId())) {
            return ReasonCode.DUPLICATED_PEER_ID;
        }

        return null;
    }

    private void onHandshakeDone(Peer peer) {
        if (isHandshakeDone.compareAndSet(false, true)) {
            channelMgr.onChannelActive(channel, peer);
            bftService.onMessage(channel, new NewHeightMessage(peer.getLatestBlockNumber() + 1));

            getNodes = exec.scheduleAtFixedRate(() -> msgQueue.sendMessage(new GetNodesMessage()),
                    channel.isInbound() ? 2 : 0, 2, TimeUnit.MINUTES);
            pingPong = exec.scheduleAtFixedRate(() -> msgQueue.sendMessage(new PingMessage()),
                    channel.isInbound() ? 1 : 0, 1, TimeUnit.MINUTES);
        } else {
            msgQueue.disconnect(ReasonCode.HANDSHAKE_EXISTS);
        }
    }
}
