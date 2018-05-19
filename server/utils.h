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

void calculateHash(StorageCloud::HashAlgorithm, const uint8_t*, int, uint8_t**, uint16_t*);
bool compareHash(const uint8_t*, uint16_t, const uint8_t*, uint16_t);
uint32_t parseSize(const uint8_t*);
std::string printHash(const uint8_t, const uint8_t*);
void encrypt(StorageCloud::EncryptionAlgorithm, const uint8_t*, uint32_t, uint8_t**, uint32_t*);
void decrypt(StorageCloud::EncryptionAlgorithm, const uint8_t*, uint32_t, uint8_t**, uint32_t*);

char getch();

#endif //SERVER_MESSAGES_H
