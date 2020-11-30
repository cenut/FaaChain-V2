package com.faa.chain.net;

import com.faa.utils.CommonUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import com.faa.chain.net.msg.DisconnectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageQueue {

    private static final Logger logger = LoggerFactory.getLogger(MessageQueue.class);

    private static final ScheduledExecutorService timer = Executors.newScheduledThreadPool(4, new ThreadFactory() {
        private final AtomicInteger cnt = new AtomicInteger(0);

        public Thread newThread(Runnable r) {
            return new Thread(r, "msg-" + cnt.getAndIncrement());
        }
    });

    private final Queue<Message> queue = new ConcurrentLinkedQueue<>();
    private final Queue<Message> prioritized = new ConcurrentLinkedQueue<>();

    private ChannelHandlerContext ctx;
    private ScheduledFuture<?> timerTask;

    private AtomicBoolean isClosed = new AtomicBoolean(false);

    /**
     * Activates this message queue and binds it to the channel.
     *
     * @param ctx
     */
    public synchronized void activate(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        this.timerTask = timer.scheduleAtFixedRate(() -> {
            try {
                nudgeQueue();
            } catch (Exception t) {
                logger.error("Exception in MessageQueue", t);
            }
        }, 10, 10, TimeUnit.MILLISECONDS);
    }

    /**
     * Deactivates this message queue.
     */
    public synchronized void deactivate() {
        this.timerTask.cancel(false);
    }

    /**
     * Returns if this message queue is idle.
     *
     * NOTE that requests are no longer kept in the queue after we send them out.
     * even through the message queue is idle, from our perspective, the peer may
     * still be busy responding our requests.
     *
     * @return true if message queues are empty, otherwise false
     */
    public boolean isIdle() {
        return size() == 0;
    }

    /**
     * Disconnects aggressively.
     *
     * @param code
     */
    public void disconnect(ReasonCode code) {
        logger.debug("Actively closing the connection: reason = " + code);

        if (isClosed.compareAndSet(false, true)) {
            ctx.writeAndFlush(new DisconnectMessage(code)).addListener((ChannelFutureListener) future -> ctx.close());
        }
    }

    /**
     * Adds a message to the sending queue.
     *
     * @param msg
     *            the message to be sent
     * @return true if the message is successfully added to the queue, otherwise
     *         false
     */
    public boolean sendMessage(Message msg) {
        logger.info("Sending Message {}", msg.toString());
        if (size() >= CommonUtil.NET_MAX_MESSAGE_QUEUE_SIZE) {
            disconnect(ReasonCode.MESSAGE_QUEUE_FULL);
            return false;
        }
        if (CommonUtil.netPrioritizedMessages.contains(msg.getCode())) {
            prioritized.add(msg);
        } else {
            queue.add(msg);
        }
        return true;
    }

    /**
     * Returns the number of messages in queue.
     *
     * @return
     */
    public int size() {
        return queue.size() + prioritized.size();
    }

    protected void nudgeQueue() {
        // 1000 / 10 * 5 = 500 messages per second
        int n = Math.min(5, size());
        if (n == 0) {
            return;
        }

        for (int i = 0; i < n; i++) {
            Message msg = !prioritized.isEmpty() ? prioritized.poll() : queue.poll();

            logger.debug("Wiring message: {}", msg.toString());
            ctx.write(msg).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        }

        ctx.flush();
    }
}
