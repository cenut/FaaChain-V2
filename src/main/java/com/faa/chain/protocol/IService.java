package com.faa.chain.protocol;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import com.faa.chain.protocol.core.Request;
import com.faa.chain.protocol.core.Response;

/**
 * Services API.
 */
public interface IService {
    <T extends Response> T send(
            Request request, Class<T> responseType) throws IOException;

    <T extends Response> CompletableFuture<T> sendAsync(
            Request request, Class<T> responseType);
}
