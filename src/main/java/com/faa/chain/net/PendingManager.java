/**
 * Copyright (c) 2019 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.faa.chain.net;

import com.faa.chain.core.Block;
import com.faa.chain.node.Transaction;
import com.faa.chain.core.TransactionExecutor;
import com.faa.chain.core.TransactionResult;
import com.faa.utils.CommonUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.faa.chain.net.msg.TransactionMessage;
import com.faa.chain.utils.ByteArray;
import com.faa.chain.utils.Bytes;
import com.faa.chain.utils.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Pending manager maintains all unconfirmed transactions, either from kernel or
 * network. All transactions are evaluated and propagated to peers if success.
 *
 * Note that: the transaction results in pending manager are not reliable for VM
 * transactions because these are evaluated against a dummy block. Nevertheless,
 * transactions included by the pending manager are eligible for inclusion in
 * block proposing phase.
 *
 * TODO: sort transaction queue by fee, and other metrics
 */

@Component
public class PendingManager {

    private static final Logger logger = LoggerFactory.getLogger(PendingManager.class);

    private static final ThreadFactory factory = new ThreadFactory() {

        private final AtomicInteger cnt = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "pending-" + cnt.getAndIncrement());
        }
    };

    @Resource
    private ChannelManager channelManager;

    public static final long ALLOWED_TIME_DRIFT = TimeUnit.HOURS.toMillis(2);

    private static final int QUEUE_SIZE_LIMIT = 128 * 1024;
    private static final int VALID_TXS_LIMIT = 16 * 1024;
    private static final int LARGE_NONCE_TXS_LIMIT = 32 * 1024;
    private static final int PROCESSED_TXS_LIMIT = 128 * 1024;

    private Block dummyBlock;

    private final LinkedHashMap<String, Transaction> queue = new LinkedHashMap<>();

    private final ArrayList<PendingTransaction> validTxs = new ArrayList<>();

    private final Cache<String, Transaction> largeNonceTxs = Caffeine.newBuilder().maximumSize(LARGE_NONCE_TXS_LIMIT)
            .build();

    private final Cache<String, Long> processedTxs = Caffeine.newBuilder().maximumSize(PROCESSED_TXS_LIMIT).build();

    private ScheduledExecutorService exec;

    private ScheduledFuture<?> validateFuture;

    private volatile boolean isRunning;

    @PostConstruct
    void init() {
        this.exec = Executors.newSingleThreadScheduledExecutor(factory);
    }

    /**
     * Starts this pending manager.
     */
    public synchronized void start() {
        if (!isRunning) {
            this.validateFuture = exec.scheduleAtFixedRate(this::run, 2, 2, TimeUnit.MILLISECONDS);

            logger.info("Pending manager started");
            this.isRunning = true;
        }
    }

    /**
     * Shuts down this pending manager.
     */
    public synchronized void stop() {
        if (isRunning) {
            validateFuture.cancel(true);

            logger.info("Pending manager stopped");
            isRunning = false;
        }
    }

    /**
     * Returns whether the pending manager is running or not.
     *
     * @return
     */
    public synchronized boolean isRunning() {
        return isRunning;
    }

    /**
     * Returns a copy of the queue, for test purpose only.
     *
     * @return
     */
    public  List<Transaction> getQueue() {
        return new ArrayList<>(queue.size());
    }

    /**
     * Adds a transaction to the queue, which will be validated later by the
     * background worker. Transaction may get rejected if the queue is full.
     *
     * @param tx
     */
    public void addTransaction(Transaction tx) {
        String hash = tx.getHash();

        logger.info("addTransaction called");

        if (queue.size() < QUEUE_SIZE_LIMIT
                && tx.validate() == 0) {
            queue.put(tx.getHash(), tx);
        }
    }

    /**
     * Returns pending transactions, limited by the given total size in bytes.
     *
     *
     * @return
     */
    public  List<PendingTransaction> getPendingTransactions(long blockGasLimit) {
        List<PendingTransaction> txs = new ArrayList<>();
        Iterator<PendingTransaction> it = validTxs.iterator();

        while (it.hasNext() && blockGasLimit > 0) {
            PendingTransaction tx = it.next();

            txs.add(tx);
        }

        return txs;
    }

    /**
     * Returns all pending transactions.
     *
     * @return
     */
    public  List<PendingTransaction> getPendingTransactions() {
        return getPendingTransactions(Long.MAX_VALUE);
    }

    /**
     * Resets the pending state and returns all pending transactions.
     *
     * @return
     */
    public  List<PendingTransaction> reset() {
        List<PendingTransaction> txs = new ArrayList<>(validTxs);
        validTxs.clear();

        return txs;
    }

    public  void onBlockAdded(Block block) {
        if (isRunning) {
            long t1 = TimeUtil.currentTimeMillis();

            List<PendingTransaction> txs = reset();

            long accepted = 0;
            for (PendingTransaction tx : txs) {
                accepted += processTransaction(tx.transaction, true, false).accepted;
            }

            long t2 = TimeUtil.currentTimeMillis();
            logger.info("Execute pending transactions: # txs = {} / {},  time = {} ms", accepted, txs.size(), t2 - t1);
        }
    }

    public void run() {
        Iterator<Map.Entry<String, Transaction>> iterator = queue.entrySet().iterator();

        while (validTxs.size() < VALID_TXS_LIMIT && iterator.hasNext()) {
            Map.Entry<String, Transaction> entry = iterator.next();
            iterator.remove();

            logger.info("call processTransaction");
            int accepted = processTransaction(entry.getValue(), false, false).accepted;
            processedTxs.put(entry.getKey(), TimeUtil.currentTimeMillis());

            if (accepted > 0) {
                break;
            }
        }
    }

    /**
     * Validates the given transaction and add to pool if success.
     *
     * @param tx
     *            a transaction
     * @param isIncludedBefore
     *            whether the transaction is included before
     * @param isFromThisNode
     *            whether the transaction is from this node
     * @return the number of transactions that have been included
     */
    protected ProcessingResult processTransaction(Transaction tx, boolean isIncludedBefore, boolean isFromThisNode) {

        int cnt = 0;
        long now = TimeUtil.currentTimeMillis();

        if (tx.getTimestamp() < now - CommonUtil.poolMaxTransactionTimeDrift
                || tx.getTimestamp() > now + CommonUtil.poolMaxTransactionTimeDrift) {
            return new ProcessingResult(0, TransactionResult.Code.INVALID_TIMESTAMP);
        }

        while (tx != null) {
            TransactionResult result = new TransactionExecutor(false,
                    false)
                    .execute(tx, dummyBlock, 0);

            if (result.getCode().isAcceptable() || true) {

                PendingTransaction pendingTransaction = new PendingTransaction(tx, result);
                validTxs.add(pendingTransaction);
                cnt++;

                logger.info("validTxs size {} {}", validTxs.size(), validTxs);

                if (!isIncludedBefore) {
                    broadcastTransaction(tx, isFromThisNode);
                }
            } else {
                return new ProcessingResult(cnt, result.getCode());
            }

            isIncludedBefore = false;
        }

        return new ProcessingResult(cnt);
    }

    private void broadcastTransaction(Transaction tx, boolean toAllPeers) {
        List<Channel> channels = channelManager.getActiveChannels();

        int n = CommonUtil.netRelayRedundancy;
        if (!toAllPeers && channels.size() > n) {
            Collections.shuffle(channels);
            channels = channels.subList(0, n);
        }

        TransactionMessage msg = new TransactionMessage(tx);
        for (Channel c : channels) {
            if (c.isActive()) {
                c.getMessageQueue().sendMessage(msg);
            }
        }
    }

    private ByteArray createKey(byte[] acc, long nonce) {
        return ByteArray.of(Bytes.merge(acc, Bytes.of(nonce)));
    }

    /**
     * This object represents a transaction and its execution result against a
     * snapshot of local state that is not yet confirmed by the network.
     */
    public static class PendingTransaction {

        public final Transaction transaction;

        public final TransactionResult result;

        public PendingTransaction(Transaction transaction, TransactionResult result) {
            this.transaction = transaction;
            this.result = result;
        }
    }

    /**
     * This object represents the number of accepted transactions and the cause of
     * rejection by ${@link PendingManager}.
     */
    public static class ProcessingResult {

        public final int accepted;

        public final TransactionResult.Code error;

        public ProcessingResult(int accepted, TransactionResult.Code error) {
            this.accepted = accepted;
            this.error = error;
        }

        public ProcessingResult(int accepted) {
            this.accepted = accepted;
            this.error = null;
        }
    }
}
