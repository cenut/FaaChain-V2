package com.faa.chain.protocol;

import java.util.concurrent.ScheduledExecutorService;

import com.faa.chain.protocol.core.Faa;
import com.faa.chain.protocol.core.JsonRpc2_0FaaClient;

/**
 * JSON-RPC Request object building factory.
 */
public interface FaaClient extends Faa {

    /**
     * Construct a new instance.
     *
     * @param iservice service instance - i.e. HTTP or IPC
     * @return new instance
     */
    static FaaClient build(IService iservice) {
        return new JsonRpc2_0FaaClient(iservice);
    }

    /**
     * Construct a new instance.
     *
     * @param iservice service instance - i.e. HTTP or IPC
     * @param pollingInterval polling interval for responses from network nodes
     * @param scheduledExecutorService executor service to use for scheduled tasks.
     *                                 <strong>You are responsible for terminating this thread
     *                                 pool</strong>
     * @return new instance
     */
    static FaaClient build(
            IService iservice, long pollingInterval,
            ScheduledExecutorService scheduledExecutorService) {
        return new JsonRpc2_0FaaClient(iservice, pollingInterval, scheduledExecutorService);
    }

    /**
     * Shutdowns a instance and closes opened resources.
     */
    void shutdown();
}
