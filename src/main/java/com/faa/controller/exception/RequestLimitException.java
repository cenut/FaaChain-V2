package com.faa.controller.exception;

public class RequestLimitException extends Exception {
    private static final long serialVersionUID = 1364225358754654702L;

    public RequestLimitException() {
        super("url 请求频率超出设定的限制");
    }

    public RequestLimitException(String message) {
        super(message);
    }

}
