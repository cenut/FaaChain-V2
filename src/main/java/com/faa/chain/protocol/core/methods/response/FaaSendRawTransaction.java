package com.faa.chain.protocol.core.methods.response;

import com.faa.chain.protocol.core.Response;

/**
 * faa_sendRawTransaction.
 */
public class FaaSendRawTransaction extends Response<String> {
    public String getTransactionHash() {
        return getResult();
    }
}
