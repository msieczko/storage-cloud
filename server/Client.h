#ifndef SERVER_CLIENT_H
#define SERVER_CLIENT_H

#include "main.h"
#include "utils.h"

using namespace std;
using namespace StorageCloud;

class Client {
private:
    int socket;
    string username = "";
    connection* this_connection;

    HashAlgorithm getHashAlgorithm();
    EncryptionAlgorithm getEncryptionAlgorithm();
    void setEncryptionAlgorithm(EncryptionAlgorithm);
    bool getNBytes(int, uint8_t*);
    bool processMessage(uint8_t*, int);
    bool parseMessage(uint8_t*, int, uint8_t*, uint8_t**, uint32_t*);
    bool processCommand(Command*);
    bool processHandshake(Handshake*);
    bool sendServerResponse(const ServerResponse*);
    bool prepareDataToSend(uint8_t*, uint32_t);
    bool getMessage();

public:
    Client(int, connection*);
    void loop(volatile bool*);
};

#endif //SERVER_CLIENT_H
