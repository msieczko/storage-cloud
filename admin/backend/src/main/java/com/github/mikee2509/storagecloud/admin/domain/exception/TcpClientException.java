package com.github.mikee2509.storagecloud.admin.domain.exception;

import java.io.IOException;

public class TcpClientException extends IOException {
    public TcpClientException(String message) {
        super(message);
    }
}
