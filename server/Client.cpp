//
// Created by milosz on 18.05.18.
//

#include "Client.h"

using namespace std;
using namespace StorageCloud;

Client::Client(int sock) {
    socket = sock;
}

bool Client::getNBytes(const int n, uint8_t buf[]) {
    int received = 0;
    int last_received = 0;

    while (received != n) {
        last_received = (int) recv(socket, buf, (size_t) (n - received), MSG_WAITALL);
        if (last_received == 0) {
            printf("no new data, closing\n");
            break;
        }

        if (last_received < 0) {
            perror("ERROR reading from socket");
            break;
        }

        received += last_received;
    }

    return (received == n);
}

bool Client::processMessage(uint8_t buf[], int len) {
    uint8_t msg_type;

    uint8_t* parsed_msg = nullptr;
    uint32_t parsed_len;

    bool parsed = parseMessage(buf, len, &msg_type, &parsed_msg, &parsed_len);

    if(!parsed || parsed_len == 0) {
        cout<<"There was an error during message parsing"<<endl;
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
        cout<<"Error: unknown message type!"<<endl;
    }

    delete parsed_msg;
}

bool Client::parseMessage(uint8_t buf[], int len, uint8_t* msg_type, uint8_t** parsed_data, uint32_t* parsed_len) {
    EncodedMessage msg;
    msg.ParseFromArray(buf, len);
    cout<<"Parsing message"<<endl;
    cout<<"size: "<<msg.datasize()<<endl;
    cout<<"hash: "<<printHash(msg.hashalgorithm(), (uint8_t*) msg.hash().c_str())<<"("<<msg.hash().length()<<", "<<HASH_SIZE[msg.hashalgorithm()]<<")"<<endl;
    cout<<"data: "<<msg.data()<<"("<<msg.data().length()<<")"<<endl;

    if(!msg.data().length() || msg.hash().length() != HASH_SIZE[msg.hashalgorithm()]) {
        cout<<"wrong data or hash length"<<endl;
        return false;
    }

    *parsed_len = 0; //decrypted data size

    EncryptionAlgorithm decrypt_alg = encryption_algorithm;
    if(msg.type() == MessageType::HANDSHAKE) {
        decrypt_alg = EncryptionAlgorithm::NOENCRYPTION;
    }

    decrypt(decrypt_alg, (uint8_t*) msg.data().c_str(), msg.datasize(), parsed_data, parsed_len);

    if(msg.datasize() != *parsed_len) {
        cout<<"wrong data length"<<endl;
        return false;
    }

    cout<<"decrypted data: "<<flush;

    for(int i=0; i<*parsed_len; i++) {
        cout<<(char) (*parsed_data)[i]<<flush;
    }
    cout<<endl;

    uint8_t* hash = nullptr;
    uint16_t hash_size;

    calculateHash(msg.hashalgorithm(), *parsed_data, *parsed_len, &hash, &hash_size);

    bool hash_ok = compareHash(hash, hash_size, (uint8_t*) msg.hash().c_str(), msg.hash().length());

    if(!hash_ok) {
        cout<<"wrong hash"<<endl;
        cout<<"should be "<<printHash(msg.hashalgorithm(), (uint8_t*) msg.hash().c_str())<<endl;
        cout<<"got       "<<printHash(msg.hashalgorithm(), hash)<<endl;
        delete hash;
        return false;
    }

    cout<<"Received message type: "<<MessageType_Name(msg.type())<<" ("<<msg.type()<<")"<<endl;

    *msg_type = msg.type();

    delete hash;
    return true;
}

bool Client::processCommand(Command* cmd) {
    cout<<"received command '"<<CommandType_Name(cmd->type())<<"' ("<<cmd->type()<<"), with "<<cmd->params_size()<<" params"<<endl;

    ServerResponse res;

    if(cmd->type() == CommandType::LOGIN) {
        res.set_type(ResponseType::LOGGED);
        sendServerResponse(&res);
    }
}

bool Client::processHandshake(Handshake* handshake) {
    cout<<"Setting encryption to "<<EncryptionAlgorithm_Name(handshake->encryptionalgorithm())<<endl;

    encryption_algorithm =  handshake->encryptionalgorithm();

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

    calculateHash(hashing_algorithm, in_buf, len, &hash, &hash_len);

    encrypt(encryption_algorithm, in_buf, len, &data, &size);

    msg.set_hash((char*)hash, hash_len);
    msg.set_datasize(len);
    msg.set_data((char*)data, size);
    msg.set_type(MessageType::SERVER_RESPONSE);
    msg.set_hashalgorithm(hashing_algorithm);

    out_len = msg.ByteSize() + 4;

    if(out_len > MAX_PACKET_SIZE - 4) {
        cout<<"Error: message too big"<<endl;
        delete hash;
        delete data;
        return false;
    }

    out_buf = new uint8_t[out_len];

    msg.SerializeToArray(out_buf + 4, out_len - 4);

    cout<<"sending response with size: "<<out_len<<" ("<<out_len-4<<"+4)"<<endl;

    out_buf[3] = out_len & 0xFF;
    out_buf[2] = (out_len >> 8) & 0xFF;
    out_buf[1] = (out_len >> 16) & 0xFF;
    out_buf[0] = (out_len >> 24) & 0xFF;

    delete hash;
    delete data;
    return true;
}

void Client::loop(volatile bool* should_exit) {
    uint8_t msg_buf[MAX_PACKET_SIZE];
    uint8_t size_buf[4];

    while(!(*should_exit)) {
        if(!getNBytes(4, size_buf)) {
            cout<<"connection error (size)"<<endl;
            break;
        }

        uint32_t size = parseSize(size_buf);

        if(size > MAX_PACKET_SIZE) {
            cout<<"message too big"<<endl;
            break;
        }

        if(!getNBytes(size - 4, msg_buf)) {
            cout<<"connection error (msg)"<<endl;
            break;
        }

        cout<<"got all data ("<<size<<")"<<endl;

        bool response = processMessage(msg_buf, size);
    }
}