package com.github.mikee2509.storagecloud.admin.domain.exception;

public class SocketConnectionException extends RuntimeException {
    public SocketConnectionException(String message) {
        super(message);
    }

    public SocketConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
