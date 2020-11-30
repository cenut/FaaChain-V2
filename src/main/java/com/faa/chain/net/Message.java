package com.faa.chain.net;

import com.faa.chain.utils.Bytes;

public abstract class Message extends Throwable {
    /**
     * Message code.
     */
    protected final MessageCode code;

    /**
     * Response message class.
     */
    protected final Class<?> responseMessageClass;

    /**
     * Message body.
     */
    protected byte[] body;

    /**
     * Create a message instance.
     *
     * @param code
     * @param responseMessageClass
     */
    public Message(MessageCode code, Class<?> responseMessageClass) {
        this.code = code;
        this.responseMessageClass = responseMessageClass;
        this.body = Bytes.EMPTY_BYTES;
    }

    /**
     * Get the body of this message
     *
     * @return
     */
    public byte[] getBody() {
        return body;
    }

    /**
     * Get the message code
     *
     * @return
     */
    public MessageCode getCode() {
        return code;
    }

    /**
     * Get the response message class of this message.
     *
     * @return the response message, or null if this message requires no response.
     */
    public Class<?> getResponseMessageClass() {
        return responseMessageClass;
    }

    /**
     * Return the message name.
     */
    public String toString() {
        return getClass().getName();
    }
}
