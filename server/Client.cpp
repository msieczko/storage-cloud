#include "Client.h"

using namespace std;
using namespace StorageCloud;

Client::Client(int sock, connection* conn, bool* s_e, Logger* logg) {
    socket = sock;
    this_connection = conn;
    should_exit = s_e;
    logger = logg;
    id = conn->addr;
    id += ":" + to_string(conn->port);
}

HashAlgorithm Client::getHashAlgorithm() {
    return this_connection->hash_algorithm;
}

EncryptionAlgorithm Client::getEncryptionAlgorithm() {
    return this_connection->encryption;
}

void Client::setEncryptionAlgorithm(EncryptionAlgorithm newAlgorithm) {
    this_connection->encryption = newAlgorithm;
}

bool Client::getNBytes(const int n, uint8_t buf[]) {
    struct epoll_event ev, events[1];
    int epfd = epoll_create1(0), nfds;
    ev.events = EPOLLIN;
    ev.data.fd = socket;

    if (epoll_ctl(epfd, EPOLL_CTL_ADD, socket, &ev) == -1) {
        perror("epoll_ctl: listen_sock");
        return false;
    }

    int received = 0;
    int last_received = 0;

    while (received != n && !(*should_exit)) {
        nfds = epoll_wait(epfd, events, 1, 1000);

        if (nfds == -1) {
//            perror("epoll_wait");
            break;
        }

        if (nfds > 0) {
            last_received = (int) recv(socket, buf, (size_t) (n - received), MSG_DONTWAIT);
            if (last_received == 0) {
                if(errno != EWOULDBLOCK || errno != EAGAIN) {
                    logger->info(id, "no new data, closing");
                    break;
                }
            }

            if (last_received < 0) {
                logger->err(id, "error while reading from socket", errno);
                break;
            }

            received += last_received;
        }
    }

    return (received == n);
}

bool Client::processMessage(uint8_t buf[], int len) {
    MessageType msg_type;

    uint8_t* parsed_msg = nullptr;
    uint32_t parsed_len;

    bool parsed = parseMessage(buf, len, &msg_type, &parsed_msg, &parsed_len);

    if(!parsed || parsed_len == 0) {
        logger->warn(id, "There was an error during message parsing");
        return false;
    }


    if(msg_type == MessageType::COMMAND) {
        Command cmd;
        cmd.ParseFromArray(parsed_msg, parsed_len);
        processCommand(&cmd);
    } else if(msg_type == MessageType::HANDSHAKE) {
        Handshake handshake;
        handshake.ParseFromArray(parsed_msg, parsed_len);
        processHandshake(&handshake);
    } else {
        logger->err(id, "Error: unknown message type! (" + MessageType_Name(msg_type) + ")");
    }

    delete parsed_msg;
}

bool Client::parseMessage(uint8_t buf[], int len, MessageType* msg_type, uint8_t** parsed_data, uint32_t* parsed_len) {
    EncodedMessage msg;
    msg.ParseFromArray(buf, len);
    logger->log(id, "Parsing message");
    logger->log(id, "size: " + to_string(msg.datasize()));
    logger->log(id, "hash: " + printHash(msg.hashalgorithm(), (uint8_t*) msg.hash().c_str()));
    logger->log(id, "data length: " + to_string(msg.data().length()));

    if(!msg.data().length() || msg.hash().length() != HASH_SIZE[msg.hashalgorithm()]) {
        logger->log(id, "wrong data or hash length");
        return false;
    }

    *parsed_len = 0; //decrypted data size

    EncryptionAlgorithm decrypt_alg = getEncryptionAlgorithm();
    if(msg.type() == MessageType::HANDSHAKE) {
        decrypt_alg = EncryptionAlgorithm::NOENCRYPTION;
    }

    decrypt(decrypt_alg, (uint8_t*) msg.data().c_str(), msg.datasize(), parsed_data, parsed_len);

    if(msg.datasize() != *parsed_len) {
        logger->warn(id, "wrong data length");
        return false;
    }

//    cout<<"decrypted data: "<<flush;
//
//    for(int i=0; i<*parsed_len; i++) {
//        cout<<(char) (*parsed_data)[i]<<flush;
//    }
//    cout<<endl;

    uint8_t* hash = nullptr;
    uint16_t hash_size;

    calculateHash(msg.hashalgorithm(), *parsed_data, *parsed_len, &hash, &hash_size);

    bool hash_ok = compareHash(hash, hash_size, (uint8_t*) msg.hash().c_str(), msg.hash().length());

    if(!hash_ok) {
        logger->warn(id, "wrong hash");
        logger->warn(id, "should be " + printHash(msg.hashalgorithm(), (uint8_t*) msg.hash().c_str()));
        logger->warn(id, "got       " + printHash(msg.hashalgorithm(), hash));
        delete hash;
        return false;
    }

    logger->log(id, "Received message type: " + MessageType_Name(msg.type()) + " (" + to_string(msg.type()) + ")");

    *msg_type = msg.type();

    delete hash;
    return true;
}

bool Client::processCommand(Command* cmd) {
    logger->log(id, "Received command '" + CommandType_Name(cmd->type()) + "' (" + to_string(cmd->type()) +
                    "), with " + to_string(cmd->params_size()) + " params");

    ServerResponse res;

    if(cmd->type() == CommandType::LOGIN) {
        res.set_type(ResponseType::LOGGED);
        sendServerResponse(&res);
    }
}

bool Client::processHandshake(Handshake* handshake) {
    logger->info(id, "Setting encryption to " + EncryptionAlgorithm_Name(handshake->encryptionalgorithm()));

    setEncryptionAlgorithm(handshake->encryptionalgorithm());

    ServerResponse res;
    res.set_type(ResponseType::OK);
    sendServerResponse(&res);
}

bool Client::sendServerResponse(const ServerResponse* res) {
    uint32_t data_len = res->ByteSize();
    uint8_t* data = new uint8_t[data_len];
    res->SerializeToArray(data, data_len);
    prepareDataToSend(data, data_len);
    delete data;
}

bool Client::prepareDataToSend(uint8_t in_buf[], uint32_t len) {
    EncodedMessage msg;

    uint8_t* hash = nullptr;
    uint16_t hash_len;
    uint8_t* data = nullptr;
    uint32_t size = 0;
    uint8_t* out_buf = nullptr;
    uint32_t out_len = 0;

    calculateHash(getHashAlgorithm(), in_buf, len, &hash, &hash_len);

    encrypt(getEncryptionAlgorithm(), in_buf, len, &data, &size);

    msg.set_hash((char*)hash, hash_len);
    msg.set_datasize(len);
    msg.set_data((char*)data, size);
    msg.set_type(MessageType::SERVER_RESPONSE);
    msg.set_hashalgorithm(getHashAlgorithm());

    out_len = msg.ByteSize() + 4;

    if(out_len > MAX_PACKET_SIZE - 4) {
        logger->warn(id, "response message too big (" + to_string(out_len) + ">" + to_string(MAX_PACKET_SIZE + 4) + ")");
        delete hash;
        delete data;
        return false;
    }

    out_buf = new uint8_t[out_len];

    msg.SerializeToArray(out_buf + 4, out_len - 4);

    logger->log(id, "sending response with size: " + to_string(out_len) + " (" + to_string(out_len-4) + "+4)");

    out_buf[3] = out_len & 0xFF;
    out_buf[2] = (out_len >> 8) & 0xFF;
    out_buf[1] = (out_len >> 16) & 0xFF;
    out_buf[0] = (out_len >> 24) & 0xFF;

    // TODO send data

    delete hash;
    delete data;
    return true;
}

bool Client::getMessage() {
    uint8_t msg_buf[MAX_PACKET_SIZE];
    uint8_t size_buf[4];

    if(!getNBytes(4, size_buf)) {
        if(!(*should_exit))
            logger->warn(id, "connection error (size)");
        return false;
    }

    uint32_t size = parseSize(size_buf);

    if(size > MAX_PACKET_SIZE) {
        logger->err(id, "incoming message too big (" + to_string(size) + ">" + to_string(MAX_PACKET_SIZE) + ")");
        return false;
    }

    if(!getNBytes(size - 4, msg_buf)) {
        if(!(*should_exit))
            logger->err(id, "connection error while getting message body");
        return false;
    }

    logger->log(id, "got all data (" + to_string(size) + ")");

    processMessage(msg_buf, size);

    return true;
}

void Client::loop() {

    while(!(*should_exit)) {
        if (!getMessage()) {
            break;
        }
    }
}