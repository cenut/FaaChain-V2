package com.faa.chain.consensus;

import com.faa.chain.core.Block;
import com.faa.chain.core.BlockPart;
import com.faa.chain.core.SyncManager;
import com.faa.chain.net.Channel;
import com.faa.chain.net.ChannelManager;
import com.faa.chain.net.Message;
import com.faa.chain.net.ReasonCode;
import com.faa.chain.net.msg.consensus.BlockMessage;
import com.faa.chain.net.msg.consensus.BlockPartsMessage;
import com.faa.chain.net.msg.consensus.GetBlockPartsMessage;
import com.faa.chain.p2p.Peer;
import com.faa.chain.utils.TimeUtil;
import com.faa.service.BlockService;
import com.faa.utils.CommonUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
public class FaachainSync implements SyncManager {
    private static final Logger logger = LoggerFactory.getLogger(FaachainSync.class);

    private static final ThreadFactory factory = new ThreadFactory() {
        private AtomicInteger cnt = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "sync-" + cnt.getAndIncrement());
        }
    };

    private static final ScheduledExecutorService timer1 = Executors.newSingleThreadScheduledExecutor(factory);
    private static final ScheduledExecutorService timer2 = Executors.newSingleThreadScheduledExecutor(factory);

    private static final Random random = new Random();

    @Resource
    private ChannelManager channelMgr;

    @Resource
    private BlockService blockService;

    private AtomicInteger latestQueuedTask = new AtomicInteger();

    private TreeSet<Integer> toDownload = new TreeSet<>();

    private Map<Integer, Long> toReceive = new HashMap<>();

    private TreeSet<Pair<Block, Channel>> toValidate = new TreeSet<>(
            Comparator.comparingLong(o -> o.getKey().getNumber()));

    private TreeMap<Integer, Pair<Block, Channel>> toImport = new TreeMap<>();

    private AtomicLong begin = new AtomicLong();
    private AtomicLong current = new AtomicLong();
    private AtomicLong target = new AtomicLong();

    private final Object lock = new Object();

    private Instant beginningInstant;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private Set<String> badPeers = new HashSet<>();

    @Override
    public void start(long targetHeight) {
        if (isRunning.compareAndSet(false, true)) {
            beginningInstant = Instant.now();

            badPeers.clear();

            logger.info("Syncing started, best known block = {}", targetHeight - 1);

            synchronized (lock) {
                toDownload.clear();
                toReceive.clear();
                toValidate.clear();
                toImport.clear();

                begin.set(blockService.getLatestBlockNumber() + 1);
                current.set(blockService.getLatestBlockNumber() + 1);
                target.set(targetHeight);
                latestQueuedTask.set(blockService.getLatestBlockNumber());
                growToDownloadQueue();
            }

            ScheduledFuture<?> download = timer1.scheduleAtFixedRate(this::download, 0, 1, TimeUnit.MILLISECONDS);
            ScheduledFuture<?> process = timer2.scheduleAtFixedRate(this::process, 0, 1, TimeUnit.MILLISECONDS);

            while (isRunning.get()) {
                synchronized (isRunning) {
                    try {
                        isRunning.wait(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.info("Sync manager got interrupted");
                        break;
                    }
                }
            }

            download.cancel(true);
            process.cancel(false);

            Instant end = Instant.now();
            logger.info("Syncing finished, took {}", TimeUtil.formatDuration(Duration.between(beginningInstant, end)));
        }
    }

    @Override
    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            synchronized (isRunning) {
                isRunning.notifyAll();
            }
        }
    }

    @Override
    public boolean isRunning() {
        return isRunning.get();
    }

    protected void addBlock(Block block, Channel channel) {
        synchronized (lock) {
            if (toDownload.remove(block.getNumber())) {
                growToDownloadQueue();
            }
            toReceive.remove(block.getNumber());
            toValidate.add(Pair.of(block, channel));
        }
    }

    @Override
    public void onMessage(Channel channel, Message msg) {
        if (!isRunning()) {
            return;
        }

        switch (msg.getCode()) {
            case BLOCK: {
                BlockMessage blockMsg = (BlockMessage) msg;
                Block block = blockMsg.getBlock();
                addBlock(block, channel);
                break;
            }
            case BLOCK_PARTS: {
                BlockPartsMessage blockPartsMsg = (BlockPartsMessage) msg;
                List<BlockPart> parts = BlockPart.decode(blockPartsMsg.getParts());
                List<byte[]> data = blockPartsMsg.getData();

                if (parts.size() != data.size()) {
                    logger.debug("Part set and data do not match");
                    break;
                }

                byte[] header = null, transactions = null, votes = null;
                for (int i = 0; i < parts.size(); i++) {
                    if (parts.get(i) == BlockPart.HEADER) {
                        header = data.get(i);
                    } else if (parts.get(i) == BlockPart.TRANSACTIONS) {
                        transactions = data.get(i);
                    } else if (parts.get(i) == BlockPart.VOTES) {
                        votes = data.get(i);
                    } else {
                        // unknown
                    }
                }

                try {
                    Block block = Block.fromComponents(header, transactions, votes);
                    addBlock(block, channel);
                } catch (Exception e) {
                    logger.debug("Failed to parse a block from components", e);
                }
                break;
            }
            default: {
                break;
            }
        }
    }

    private boolean skipVotes(long blockNumber) {
        long interval = 200L;

        boolean isPivotBlock = (blockNumber % interval == 0);
        boolean isSafeBlock = (blockNumber < target.get() - interval);

        return !isPivotBlock && isSafeBlock;
    }

    private void download() {
        if (!isRunning()) {
            return;
        }

        synchronized (lock) {
            long now = TimeUtil.currentTimeMillis();
            Iterator<Map.Entry<Integer, Long>> itr = toReceive.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<Integer, Long> entry = itr.next();

                if (entry.getValue() + CommonUtil.syncDownloadTimeout < now) {
                    logger.debug("Failed to download block #{}, expired", entry.getKey());
                    toDownload.add(entry.getKey());
                    itr.remove();
                }
            }

            if (toReceive.size() > CommonUtil.syncMaxPendingJobs) {
                logger.trace("Max pending jobs reached");
                return;
            }

            if (toDownload.isEmpty()) {
                return;
            }
            Integer task = toDownload.first();

            int pendingBlocks = toValidate.size() + toImport.size();
            if (pendingBlocks > CommonUtil.syncMaxPendingBlocks && task > toValidate.first().getKey().getNumber()) {
                logger.trace("Max pending blocks reached");
                return;
            }

            List<Channel> channels = channelMgr.getIdleChannels().stream()
                    .filter(channel -> {
                        Peer peer = channel.getRemotePeer();
                        return peer.getLatestBlockNumber() >= task
                                && !badPeers.contains(peer.getPeerId());
                    })
                    .collect(Collectors.toList());
            logger.trace("Qualified idle peers = {}", channels.size());

            if (channels.isEmpty()) {
                return;
            }

            Channel c = channels.get(random.nextInt(channels.size()));

            if (skipVotes(task)) {
                logger.trace("Requesting block #{} from {}:{}, HEADER + TRANSACTIONS", task,
                        c.getRemoteIp(),
                        c.getRemotePort());
                c.getMessageQueue().sendMessage(new GetBlockPartsMessage(task,
                        BlockPart.encode(BlockPart.HEADER, BlockPart.TRANSACTIONS)));
            } else {
                logger.trace("Requesting block #{} from {}:{}, HEADER + TRANSACTIONS + VOTES", task,
                        c.getRemoteIp(), c.getRemotePort());
                c.getMessageQueue().sendMessage(new GetBlockPartsMessage(task,
                        BlockPart.encode(BlockPart.HEADER, BlockPart.TRANSACTIONS, BlockPart.VOTES)));
            }

            if (toDownload.remove(task)) {
                growToDownloadQueue();
            }
            toReceive.put(task, TimeUtil.currentTimeMillis());
        }
    }

    /**
     * Queue new tasks sequentially starting from
     * ${@link FaachainSync#latestQueuedTask} until the size of
     * ${@link FaachainSync#toDownload} queue is greater than or equal to
     * MAX_QUEUED_JOBS
     */
    private void growToDownloadQueue() {
        if (toDownload.size() >= CommonUtil.syncMaxQueuedJobs / 2) {
            return;
        }

        for (int task = latestQueuedTask.get() + 1; //
             task < target.get() && toDownload.size() < CommonUtil.syncMaxQueuedJobs; //
             task++) {
            latestQueuedTask.accumulateAndGet(task, (prev, next) -> next > prev ? next : prev);
            if (!blockService.hasBlock(task)) {
                toDownload.add(task);
            }
        }
    }

    /**
     * Fast sync process: Validate votes only for the last block in each validator
     * set. For each block in the set, compare its hash against its child parent
     * hash. Once all hashes are validated, validate (while skipping vote
     * validation) and apply each block to the chain.
     */
    protected void process() {
        if (!isRunning()) {
            return;
        }

        long latest = blockService.getLatestBlockNumber();
        if (latest + 1 >= target.get()) {
            stop();
            return;
        }

        long checkpoint = latest + 1;
        while (skipVotes(checkpoint)) {
            checkpoint++;
        }

        synchronized (lock) {
            Iterator<Pair<Block, Channel>> iterator = toValidate.iterator();
            while (iterator.hasNext()) {
                Pair<Block, Channel> p = iterator.next();
                int n = p.getKey().getNumber();

                if (n <= latest) {
                    iterator.remove();
                } else if (n <= checkpoint) {
                    iterator.remove();
                    toImport.put(n, p);
                } else {
                    break;
                }
            }

            if (toImport.size() >= checkpoint - latest) {
                boolean valid = validateBlockHashes(latest + 1, checkpoint);

                if (valid) {
                    for (long n = latest + 1; n <= checkpoint; n++) {
                        Pair<Block, Channel> p = toImport.remove(n);
                        boolean imported = false;
                        try {
                            imported = blockService.importBlock(p.getKey(), false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (!imported) {
                            handleInvalidBlock(p.getKey(), p.getValue());
                            break;
                        }

                        if (n == checkpoint) {
                            logger.info("{}", p.getLeft());
                        }
                    }
                    current.set(blockService.getLatestBlockNumber() + 1);
                }
            }
        }
    }

    /**
     * Validate block hashes in the toImport set.
     *
     * Assuming that the whole block range is available in the set.
     *
     * @param from
     *            the start block number, inclusive
     * @param to
     *            the end block number, inclusive
     */
    protected boolean validateBlockHashes(long from, long to) {
        synchronized (lock) {
            Pair<Block, Channel> checkpoint = toImport.get(to);
            Block block = checkpoint.getKey();
            if (!blockService.validateBlockVotes(block)) {
                handleInvalidBlock(block, checkpoint.getValue());
                return false;
            }

            for (long n = to - 1; n >= from; n--) {
                Pair<Block, Channel> current = toImport.get(n);
                Pair<Block, Channel> child = toImport.get(n + 1);

                if (!Arrays.equals(current.getKey().getHash(), child.getKey().getParentHash())) {
                    handleInvalidBlock(current.getKey(), current.getValue());
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * Handle invalid block: Add block back to download queue. Remove block from all
     * other queues. Disconnect from the peer that sent the block.
     *
     * @param block
     * @param channel
     */
    protected void handleInvalidBlock(Block block, Channel channel) {
        InetSocketAddress a = channel.getRemoteAddress();
        logger.info("Invalid block, peer = {}:{}, block # = {}", a.getAddress().getHostAddress(), a.getPort(),
                block.getNumber());
        synchronized (lock) {
            toDownload.add(block.getNumber());

            toReceive.remove(block.getNumber());
            toValidate.remove(Pair.of(block, channel));
            toImport.remove(block.getNumber());
        }

        badPeers.add(channel.getRemotePeer().getPeerId());

        if (CommonUtil.syncDisconnectOnInvalidBlock) {
            channel.getMessageQueue().disconnect(ReasonCode.BAD_PEER);
        }
    }

    @Override
    public FaachainSyncProgress getProgress() {
        return new FaachainSyncProgress(
                begin.get(),
                current.get(),
                target.get(),
                Duration.between(beginningInstant != null ? beginningInstant : Instant.now(), Instant.now()));
    }

    public static class FaachainSyncProgress implements Progress {

        final long startingHeight;

        final long currentHeight;

        final long targetHeight;

        final Duration duration;

        public FaachainSyncProgress(long startingHeight, long currentHeight, long targetHeight, Duration duration) {
            this.startingHeight = startingHeight;
            this.currentHeight = currentHeight;
            this.targetHeight = targetHeight;
            this.duration = duration;
        }

        @Override
        public long getStartingHeight() {
            return startingHeight;
        }

        @Override
        public long getCurrentHeight() {
            return currentHeight;
        }

        @Override
        public long getTargetHeight() {
            return targetHeight;
        }

        @Override
        public Duration getSyncEstimation() {
            Long speed = getSpeed();
            if (speed == null || speed == 0) {
                return null;
            }

            return Duration.ofMillis(BigInteger.valueOf(getTargetHeight())
                    .subtract(BigInteger.valueOf(getCurrentHeight()))
                    .multiply(BigInteger.valueOf(speed))
                    .longValue());
        }

        private Long getSpeed() {
            long downloadedBlocks = currentHeight - startingHeight;
            if (downloadedBlocks <= 0 || duration.toMillis() == 0) {
                return null;
            }

            return BigDecimal.valueOf(duration.toMillis())
                    .divide(BigDecimal.valueOf(downloadedBlocks), MathContext.DECIMAL64)
                    .round(MathContext.DECIMAL64)
                    .longValue();
        }
    }
}
