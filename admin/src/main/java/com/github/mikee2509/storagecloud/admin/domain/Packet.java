package com.github.mikee2509.storagecloud.admin.domain;

import com.github.mikee2509.storagecloud.proto.EncryptionAlgorithm;
import com.github.mikee2509.storagecloud.proto.ServerResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Packet {
    byte[] data;

    public static PacketBuilder builder() {
        return new PacketBuilder();
    }

    public ServerResponse parseServerResponse(EncryptionAlgorithm encryptionAlgorithm) throws ServerResponseParserException {
        return ServerResponseParser.parse(data, encryptionAlgorithm);
    }
}
