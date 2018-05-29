package com.github.mikee2509.storagecloud.admin.util;

import com.github.mikee2509.storagecloud.proto.EncryptionAlgorithm;
import com.google.protobuf.ByteString;

public class EncryptionUtility {
    public static byte[] encrypt(byte[] data, EncryptionAlgorithm algorithm) {
        // TODO implement encryption
        return data;
    }

    public static ByteString decrypt(ByteString encryptedData, EncryptionAlgorithm algorithm) {
        //TODO implement decryption
        return encryptedData;
    }
}
