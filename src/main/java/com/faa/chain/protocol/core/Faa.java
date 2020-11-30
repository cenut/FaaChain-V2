package com.faa.chain.protocol.core;

import com.faa.chain.protocol.core.methods.response.*;

/**
 * Core Faa JSON-RPC API.
 */
public interface Faa {

    Request<?, FaaSendTransaction> faaSendRawTransaction(String signedTransactionData);
}
