package com.faa.chain;

import com.faa.chain.net.PendingManager;
import com.faa.service.BftService;
import com.faa.utils.CommonUtil;
import com.faa.chain.crypto.Key;
import com.faa.chain.net.ChannelManager;
import com.faa.chain.net.NodeManager;
import com.faa.chain.p2p.PeerClient;
import com.faa.chain.p2p.PeerServer;
import com.faa.chain.utils.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * FaaChain启动类，初始化Peer client，Channel Manager，Node Manager以及Peer Server
 */

@Component
public class Starter {

    private static final Logger logger = LoggerFactory.getLogger(Starter.class);

    public enum State {
        STOPPED, BOOTING, RUNNING, STOPPING
    }

    protected State state = State.STOPPED;

    protected Key coinbase;

    @Resource
    protected PeerClient client;
    @Resource
    protected NodeManager nodeMgr;
    @Resource
    protected PeerServer p2pServer;
    @Resource
    protected PendingManager pendingManager;
    @Resource
    protected BftService bftService;

    @PostConstruct
    public synchronized void start() {
        if (state != State.STOPPED) {
            return;
        } else {
            state = State.BOOTING;
        }

        coinbase = CommonUtil.coinbase;

        logger.info(CommonUtil.getClientId());
        logger.info("System booting up: network = {}, networkVersion = {}, coinbase = {}", CommonUtil.network,
                CommonUtil.networkVersion, coinbase);

        TimeUtil.startNtpProcess();

        // 启动node manager，node manager通过channelMgr.getActiveAddresses获取活跃的channel列表
        nodeMgr.start();

        // 启动 pendingManager
        pendingManager.start();

        // 启动p2p server
        p2pServer.start();

        // run BFT service
        logger.info("run broadcaster");
        bftService.runBftBroadcaster();
        logger.info("run timer");
        bftService.runBftTimer();
        logger.info("run event loop");
        bftService.runBftEventLoop();
        logger.info("enter new height");
        bftService.enterNewHeight();

        state = State.RUNNING;
    }

    public synchronized void stop() {
        if (state != State.RUNNING) {
            return;
        } else {
            state = State.STOPPING;
        }

        p2pServer.stop();
        client.close();

        state = State.STOPPED;
    }

    /**
     * Returns the kernel state.
     *
     * @return
     */
    public State state() {
        return state;
    }
}
