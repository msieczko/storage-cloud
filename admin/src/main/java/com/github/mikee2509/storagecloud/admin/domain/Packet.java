package com.github.mikee2509.storagecloud.admin.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Packet {
    byte[] data;

    public static PacketBuilder builder() {
        return new PacketBuilder();
    }
}
