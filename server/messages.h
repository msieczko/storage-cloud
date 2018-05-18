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

using namespace std;
using namespace StorageCloud;

class Client {
private:
    int socket;
    EncryptionAlgorithm encryption_algorithm = EncryptionAlgorithm::NOENCRYPTION;
    string username = "";
    HashAlgorithm hashing_algorithm = HashAlgorithm::H_SHA512;

    bool getNBytes(int, uint8_t*);
    uint32_t parseSize(const uint8_t*);
    bool processMessage(uint8_t*, int);
    bool parseMessage(uint8_t*, int, uint8_t*, uint8_t**, uint32_t*);
    bool processCommand(Command*);
    bool processHandshake(Handshake*);
    bool sendServerResponse(const ServerResponse*);
    bool prepareDataToSend(uint8_t*, uint32_t);

public:
    Client(int);
    void loop(volatile bool*);
};

#endif //SERVER_MESSAGES_H
