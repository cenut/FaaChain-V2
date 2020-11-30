package com.faa.chain.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.faa.chain.protocol.core.Request;
import com.faa.chain.protocol.core.Response;
import com.faa.chain.utils.Async;

/**
 * Base service implementation.
 */
public abstract class Service implements IService {

    protected final ObjectMapper objectMapper;

    public Service(boolean includeRawResponses) {
        objectMapper = ObjectMapperFactory.getObjectMapper(includeRawResponses);
    }

    protected abstract InputStream performIO(String payload) throws IOException;

    @Override
    public <T extends Response> T send(Request request, Class<T> responseType) throws IOException {
        String payload = objectMapper.writeValueAsString(request);

        try (InputStream result = performIO(payload)) {
            if (result != null) {
                return objectMapper.readValue(result, responseType);
            } else {
                return null;
            }
        }
    }

    @Override
    public <T extends Response> CompletableFuture<T> sendAsync(Request jsonRpc20Request, Class<T> responseType) {
        return Async.run(() -> send(jsonRpc20Request, responseType));
    }
}
