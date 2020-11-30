package com.faa.chain.protocol.core.methods.response;

import com.faa.chain.protocol.core.Response;

/**
 * faa_sendTransaction.
 */
public class FaaSendTransaction extends Response<String> {
    public String getTransactionHash() {
        return getResult();
    }
}
