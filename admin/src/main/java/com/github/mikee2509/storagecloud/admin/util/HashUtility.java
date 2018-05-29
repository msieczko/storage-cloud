package com.github.mikee2509.storagecloud.admin.util;

import com.github.mikee2509.storagecloud.proto.HashAlgorithm;
import com.google.protobuf.ByteString;
import org.apache.commons.codec.digest.DigestUtils;

import static org.apache.commons.codec.digest.MessageDigestAlgorithms.*;

public class HashUtility {
    public static byte[] digest(byte[] data, HashAlgorithm hashAlgorithm) {
        byte[] hash;
        switch (hashAlgorithm) {
            case H_SHA512:
                hash = new DigestUtils(SHA_512).digest(data);
                break;
            case H_SHA256:
                hash = new DigestUtils(SHA_256).digest(data);
                break;
            case H_MD5:
                hash = new DigestUtils(MD5).digest(data);
                break;
            case H_SHA1:
                hash = new DigestUtils(SHA_1).digest(data);
                break;
            case H_NOHASH:
            default:
                hash = new byte[]{};
                break;
        }
        return hash;
    }

    public static ByteString digest(ByteString data, HashAlgorithm hashAlgorithm) {
        return ByteString.copyFrom(digest(data.toByteArray(), hashAlgorithm));
    }
}
