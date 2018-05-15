#ifndef SERVER_MESSAGES_H
#define SERVER_MESSAGES_H

const uint8_t HASH_SIZE[StorageCloud::EncodedMessage::HashAlgorithm_ARRAYSIZE] = {
        0,
        0,
        SHA256_DIGEST_LENGTH,
        SHA512_DIGEST_LENGTH,
        SHA_DIGEST_LENGTH,
        MD5_DIGEST_LENGTH,
};

void parseMessage(uint8_t*, int, int);

#endif //SERVER_MESSAGES_H
