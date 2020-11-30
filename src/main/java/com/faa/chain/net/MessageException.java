package com.faa.chain.net;

import java.io.IOException;

public class MessageException extends IOException {
    private static final long serialVersionUID = 1L;

    public MessageException() {
    }

    public MessageException(String s) {
        super(s);
    }

    public MessageException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public MessageException(Throwable throwable) {
        super(throwable);
    }
}
