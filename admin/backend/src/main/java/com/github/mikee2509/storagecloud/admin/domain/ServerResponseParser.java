package com.github.mikee2509.storagecloud.admin.domain;

import com.github.mikee2509.storagecloud.admin.util.EncryptionUtility;
import com.github.mikee2509.storagecloud.admin.util.HashUtility;
import com.github.mikee2509.storagecloud.proto.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.nio.ByteBuffer;

public class ServerResponseParser {
    public static ServerResponse parse(byte[] data, EncryptionAlgorithm encryptionAlgorithm)
            throws ServerResponseParserException {
        try {
            return parsePayload(ByteBuffer.wrap(data, 4, data.length - 4), encryptionAlgorithm);
        } catch (InvalidProtocolBufferException e) {
            throw new ServerResponseParserException(e);
        }
    }

    private static ServerResponse parsePayload(ByteBuffer payload, EncryptionAlgorithm encryptionAlgorithm)
            throws InvalidProtocolBufferException, ServerResponseParserException {
        EncodedMessage encodedMessage = EncodedMessage.parseFrom(payload);
        if (encodedMessage.getType() != MessageType.SERVER_RESPONSE) {
            throw new ServerResponseParserException("Packet does not represent a server response");
        }

        ByteString encryptedData = encodedMessage.getData();
        if (encodedMessage.getDataSize() != encryptedData.size()) {
            throw new ServerResponseParserException("Size declared in EncodedMessage field doesn't match payload's " +
                    "actual size");
        }

        ByteString data = EncryptionUtility.decrypt(encryptedData, encryptionAlgorithm);

        HashAlgorithm hashAlgorithm = encodedMessage.getHashAlgorithm();
        if (hashAlgorithm == HashAlgorithm.UNRECOGNIZED || hashAlgorithm == HashAlgorithm.NULL2) {
            throw new ServerResponseParserException("Hash algorithm not set");
        }

        ByteString hash = HashUtility.digest(data, hashAlgorithm);
        if (!encodedMessage.getHash().equals(hash)) {
            throw new ServerResponseParserException("Incorrect hash");
        }

        return ServerResponse.parseFrom(data);
    }

}
