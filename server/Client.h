#ifndef SERVER_CLIENT_H
#define SERVER_CLIENT_H

#include "main.h"
#include "messages.h"

using namespace std;
using namespace StorageCloud;

class Client {
private:
    int socket;
    EncryptionAlgorithm encryption_algorithm = EncryptionAlgorithm::NOENCRYPTION;
    string username = "";
    HashAlgorithm hashing_algorithm = HashAlgorithm::H_SHA512;

    bool getNBytes(int, uint8_t*);
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

#endif //SERVER_CLIENT_H
