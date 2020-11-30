package com.faa.chain.protocol.core;

import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;

import com.faa.chain.protocol.FaaClient;
import com.faa.chain.protocol.IService;
import com.faa.chain.protocol.core.methods.response.FaaSendTransaction;
import com.faa.chain.utils.Async;

/**
 * JSON-RPC 2.0 factory implementation.
 */
public class JsonRpc2_0FaaClient implements FaaClient {

    public static final int DEFAULT_BLOCK_TIME = 15 * 1000;

    protected final IService iservice;
    private final ScheduledExecutorService scheduledExecutorService;

    public JsonRpc2_0FaaClient(IService iservice) {
        this(iservice, DEFAULT_BLOCK_TIME, Async.defaultExecutorService());
    }

    public JsonRpc2_0FaaClient(IService iservice, long pollingInterval, ScheduledExecutorService scheduledExecutorService) {
        this.iservice = iservice;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public Request<?, FaaSendTransaction> faaSendRawTransaction(String signedTransactionData) {
        return new Request<>("faa_sendRawTransaction", Arrays.asList(signedTransactionData), iservice, FaaSendTransaction.class);
    }

    @Override
    public void shutdown() {
        scheduledExecutorService.shutdown();
    }
}
