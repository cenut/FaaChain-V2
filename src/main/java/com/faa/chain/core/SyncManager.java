package com.faa.chain.core;

import com.faa.chain.net.Channel;
import com.faa.chain.net.Message;

import java.time.Duration;

public interface SyncManager {

    /**
     * Starts sync manager, and sync blocks in [height, targetHeight).
     *
     * @param targetHeight
     *            the target height, exclusive
     */
    void start(long targetHeight);

    /**
     * Stops sync manager.
     */
    void stop();

    /**
     * Returns if this sync manager is running.
     *
     * @return
     */
    boolean isRunning();

    /**
     * Callback when a message is received from network.
     *
     * @param channel
     *            the channel where the message is coming from
     * @param msg
     *            the message
     */
    void onMessage(Channel channel, Message msg);

    /**
     * Returns current synchronisation progress.
     *
     * @return a ${@link Progress} object
     */
    Progress getProgress();

    /**
     * This interface represents synchronisation progress
     */
    interface Progress {

        /**
         * @return the starting height of this sync process.
         */
        long getStartingHeight();

        /**
         * @return the current height of sync process.
         */
        long getCurrentHeight();

        /**
         * @return the target height of sync process.
         */
        long getTargetHeight();

        /**
         * @return the estimated time to complete this sync process. 30 days at maximum.
         */
        Duration getSyncEstimation();
    }
}
