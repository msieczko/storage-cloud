package com.github.mikee2509.storagecloud.admin.domain;

public class PacketBuilderException extends RuntimeException {
    private PacketBuilderException(String message) {
        super(message);
    }

    public static PacketBuilderException notProvided(String what) {
        return new PacketBuilderException(what + " not provided");
    }
}
