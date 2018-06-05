package com.github.mikee2509.storagecloud.admin.domain.exception;

public class ParsingException extends RuntimeException {
    public ParsingException(String message) {
        super(message);
    }

    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
