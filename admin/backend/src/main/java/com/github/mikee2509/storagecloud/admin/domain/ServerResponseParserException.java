package com.github.mikee2509.storagecloud.admin.domain;

import com.google.protobuf.InvalidProtocolBufferException;

public class ServerResponseParserException extends Exception {
    public ServerResponseParserException(InvalidProtocolBufferException e) {
        super(e);
    }

    public ServerResponseParserException(String msg) {
        super(msg);
    }
}
