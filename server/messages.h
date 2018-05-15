#ifndef SERVER_MESSAGES_H
#define SERVER_MESSAGES_H

const uint8_t HASH_SIZE[StorageCloud::HashAlgorithm_ARRAYSIZE] = {
        0,
        0,
        SHA256_DIGEST_LENGTH,
        SHA512_DIGEST_LENGTH,
        SHA_DIGEST_LENGTH,
        MD5_DIGEST_LENGTH,
};

struct UserCtx {
    StorageCloud::EncryptionAlgorithm encryption_algorithm;
    std::string username;
    StorageCloud::HashAlgorithm hashing_algorithm = StorageCloud::HashAlgorithm::H_SHA512;
};

bool processMessage(uint8_t*, int, UserCtx* userCtx, uint8_t**, uint32_t*);

#endif //SERVER_MESSAGES_H
