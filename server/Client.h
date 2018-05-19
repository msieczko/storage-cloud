#ifndef SERVER_CLIENT_H
#define SERVER_CLIENT_H

#include "main.h"
#include "utils.h"
#include "Logger.h"

using namespace std;
using namespace StorageCloud;

class Client {
private:
    int socket;
    string username = "";
    connection* this_connection;
    bool* should_exit;
    Logger* logger;
    std::string id;

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
    Client(int, connection*, bool*, Logger*);
    void loop();
};

#endif //SERVER_CLIENT_H
